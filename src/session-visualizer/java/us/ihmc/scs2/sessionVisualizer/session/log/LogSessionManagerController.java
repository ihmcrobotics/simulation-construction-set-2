package us.ihmc.scs2.sessionVisualizer.session.log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXToggleButton;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import us.ihmc.log.LogTools;
import us.ihmc.robotDataLogger.logger.LogPropertiesReader;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.managers.BackgroundExecutorManager;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.session.SessionControlsController;
import us.ihmc.scs2.sessionVisualizer.tools.CropSlider;
import us.ihmc.scs2.sessionVisualizer.tools.JavaFXMissingTools;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class LogSessionManagerController implements SessionControlsController
{
   private static final double THUMBNAIL_WIDTH = 200.0;

   private static final String LOG_FILE_KEY = "logFilePath";

   @FXML
   private AnchorPane mainPane;
   @FXML
   private JFXSpinner loadingSpinner;
   @FXML
   private JFXButton openSessionButton, endSessionButton;
   @FXML
   private Label sessionNameLabel, dateLabel, logPathLabel;
   @FXML
   private HBox cropControlsContainer;
   @FXML
   private JFXToggleButton showTrimsButton;
   @FXML
   private JFXButton resetTrimsButton, cropAndExportButton;
   @FXML
   private CropSlider logPositionSlider;
   @FXML
   private Pane cropProgressMonitorPane;
   @FXML
   private TitledPane thumbnailsTitledPane;
   @FXML
   private FlowPane videoThumbnailPane;

   private final ObjectProperty<MultiVideoViewer> multiVideoViewerProperty = new SimpleObjectProperty<>(this, "multiVideoThumbnailViewer", null);
   private final ObjectProperty<LogSession> activeSessionProperty = new SimpleObjectProperty<>(this, "activeSession", null);
   private final ObjectProperty<YoVariableLogCropper> logCropperProperty = new SimpleObjectProperty<>(this, "logCropper", null);

   private BackgroundExecutorManager backgroundExecutorManager;

   private Stage stage;

   @Override
   public void initialize(SessionVisualizerToolkit toolkit)
   {
      stage = new Stage();

      backgroundExecutorManager = toolkit.getBackgroundExecutorManager();

      logPositionSlider.setValueFactory(param -> new TimeStringBinding(param.valueProperty(), position ->
      {
         if (activeSessionProperty.get() == null)
            return 0;
         LogDataReader logDataReader = activeSessionProperty.get().getLogDataReader();
         return logDataReader.getRelativeTimestamp(position.intValue());
      }));

      ChangeListener<? super LogSession> activeSessionListener = (o, oldValue, newValue) ->
      {
         if (oldValue != null)
         {
            LogDataReader logDataReader = oldValue.getLogDataReader();
            logDataReader.getTimestamp().removeAllVariableChangedListeners();
         }

         if (newValue == null)
         {
            sessionNameLabel.setText("N/D");
            dateLabel.setText("N/D");
            logPathLabel.setText("N/D");
            endSessionButton.setDisable(true);
            logPositionSlider.setDisable(true);
            showTrimsButton.setSelected(false);
            cropControlsContainer.setDisable(true);
            multiVideoViewerProperty.set(null);
            logCropperProperty.set(null);
         }
         else
         {
            File logDirectory = newValue.getLogDirectory();
            LogDataReader logDataReader = newValue.getLogDataReader();
            LogPropertiesReader logProperties = newValue.getLogProperties();

            sessionNameLabel.setText(newValue.getSessionName());
            dateLabel.setText(logProperties.getTimestampAsString());
            logPathLabel.setText(logDirectory.getAbsolutePath());
            endSessionButton.setDisable(false);
            logPositionSlider.setDisable(false);
            logPositionSlider.setValue(0.0);
            logPositionSlider.setMin(0.0);
            logPositionSlider.setMax(logDataReader.getNumberOfEntries() - 1);
            cropControlsContainer.setDisable(false);
            MultiVideoDataReader multiReader = new MultiVideoDataReader(logDirectory, logProperties, backgroundExecutorManager);
            multiReader.readVideoFrameNow(logDataReader.getTimestamp().getLongValue());
            logDataReader.getTimestamp().addVariableChangedListener(v -> multiReader.readVideoFrameInBackground(v.getValueAsLongBits()));
            multiVideoViewerProperty.set(new MultiVideoViewer(stage, videoThumbnailPane, multiReader, THUMBNAIL_WIDTH));
            logCropperProperty.set(new YoVariableLogCropper(multiReader, logDirectory, logProperties));
            boolean logHasVideos = multiReader.getNumberOfVideos() > 0;
            thumbnailsTitledPane.setText(logHasVideos ? "Logged videos" : "No video");
            thumbnailsTitledPane.setExpanded(logHasVideos);
            thumbnailsTitledPane.setDisable(!logHasVideos);
            Platform.runLater(() -> stage.sizeToScene());
         }
      };

      AtomicBoolean logPositionUpdate = new AtomicBoolean(true);

      logPositionSlider.valueProperty().addListener((o, oldValue, newValue) ->
      {
         LogSession logSession = activeSessionProperty.get();
         if (logSession == null || logPositionUpdate.get())
            return;

         logSession.submitLogPositionRequest(newValue.intValue());
      });

      AtomicBoolean sliderFeedbackEnabled = new AtomicBoolean(true);

      logPositionSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> sliderFeedbackEnabled.set(false));
      logPositionSlider.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> sliderFeedbackEnabled.set(false));
      logPositionSlider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> sliderFeedbackEnabled.set(true));

      Consumer<YoBufferPropertiesReadOnly> logPositionUpdateListener = properties ->
      {
         LogSession logSession = activeSessionProperty.get();

         if (sliderFeedbackEnabled.get())
         {
            Platform.runLater(() ->
            {
               if (logSession == null || logSession.getLogDataReader() == null)
                  return;

               logPositionUpdate.set(true);
               logPositionSlider.setValue(logSession.getLogDataReader().getCurrentLogPosition());
               logPositionUpdate.set(false);
            });
         }
      };

      activeSessionProperty.addListener((o, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.removeCurrentBufferPropertiesListener(logPositionUpdateListener);
         if (newValue != null)
            newValue.addCurrentBufferPropertiesListener(logPositionUpdateListener);
      });

      multiVideoViewerProperty.addListener((o, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.stop();
         if (newValue != null)
            newValue.start();
      });

      activeSessionProperty.addListener(activeSessionListener);
      activeSessionListener.changed(null, null, null);

      thumbnailsTitledPane.expandedProperty().addListener((o, oldValue, newValue) -> Platform.runLater(stage::sizeToScene));

      logPositionSlider.showTrimProperty().bind(showTrimsButton.selectedProperty());
      logPositionSlider.showTrimProperty().addListener((o, oldValue, newValue) ->
      {
         if (newValue)
            resetTrims();
      });
      resetTrimsButton.disableProperty().bind(showTrimsButton.selectedProperty().not());
      cropAndExportButton.disableProperty().bind(showTrimsButton.selectedProperty().not());
      cropProgressMonitorPane.getChildren().addListener((ListChangeListener<Node>) c ->
      {
         c.getList().forEach(node -> JavaFXMissingTools.setAnchorConstraints(node, 0.0));
         stage.sizeToScene();
      });

      openSessionButton.disableProperty().bind(loadingSpinner.visibleProperty());
      openSessionButton.setOnAction(e ->
      {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setInitialDirectory(SessionVisualizerIOTools.getDefaultFilePath(LOG_FILE_KEY));
         fileChooser.getExtensionFilters().add(new ExtensionFilter("Log property file", "*.log"));
         fileChooser.setTitle("Choose log directory");
         File result = fileChooser.showOpenDialog(stage);
         if (result == null)
            return;

         unloadSession();
         setIsLoading(true);

         backgroundExecutorManager.executeInBackground(() ->
         {
            LogSession newSession;
            try
            {
               LogTools.info("Creating log session.");
               newSession = new LogSession(result.getParentFile(), null); // TODO Need a progress window
               LogTools.info("Created log session.");
               Platform.runLater(() -> activeSessionProperty.set(newSession));
               SessionVisualizerIOTools.setDefaultFilePath(LOG_FILE_KEY, result);
            }
            catch (IOException ex)
            {
               ex.printStackTrace();
               setIsLoading(false);
            }
         });
      });

      endSessionButton.setOnAction(e ->
      {
         LogSession logSession = activeSessionProperty.get();
         if (logSession != null)
            logSession.shutdownSession();
         activeSessionProperty.set(null);
      });

      stage.setScene(new Scene(mainPane));
      stage.setTitle("Log session controls");
      stage.getIcons().add(SessionVisualizerIOTools.LOG_SESSION_IMAGE);
      toolkit.getMainWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> shutdown());
      stage.show();
   }

   public void setIsLoading(boolean isLoading)
   {
      loadingSpinner.setVisible(isLoading);
   }

   @FXML
   public void resetTrims()
   {
      logPositionSlider.setTrimStartValue(0.0);
      logPositionSlider.setTrimEndValue(logPositionSlider.getMax());
   }

   @FXML
   public void cropAndExport() throws IOException
   {
      YoVariableLogCropper logCropper = logCropperProperty.get();
      if (logCropper == null)
         return;

      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setInitialDirectory(SessionVisualizerIOTools.getDefaultFilePath());
      File destination = directoryChooser.showDialog(stage);
      if (destination == null)
         return;
      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.LOG_CROP_PROGRESS_PANE_FXML_URL);
      loader.load();
      LogCropProgressController controller = loader.getController();
      controller.initialize(cropProgressMonitorPane);

      backgroundExecutorManager.executeInBackground(() -> logCropper.crop(destination,
                                                                          (int) logPositionSlider.getTrimStartValue(),
                                                                          (int) logPositionSlider.getTrimEndValue(),
                                                                          controller));
   }

   @Override
   public void shutdown()
   {
      stage.close();
   }

   @Override
   public Stage getStage()
   {
      return stage;
   }

   @Override
   public ReadOnlyObjectProperty<? extends Session> activeSessionProperty()
   {
      return activeSessionProperty;
   }

   @Override
   public void unloadSession()
   {
      if (activeSessionProperty.get() != null)
      {
         activeSessionProperty.get().shutdownSession();
         activeSessionProperty.set(null);
      }
   }

   @Override
   public void notifySessionLoaded()
   {
      setIsLoading(false);
   }

   private static class TimeStringBinding extends StringBinding
   {
      private ObservableLongValue observableNanoTime;

      public <N extends Number> TimeStringBinding(ObservableValue<N> time, ToLongFunction<N> conversionToNano)
      {
         LongProperty nanoTimeProperty = new SimpleLongProperty(this, "nanoTime", 0);
         time.addListener((o, oldValue, newValue) -> nanoTimeProperty.set(conversionToNano.applyAsLong(newValue)));
         observableNanoTime = nanoTimeProperty;
         observableNanoTime.addListener((o, oldValue, newValue) -> invalidate());
      }

      @Override
      protected String computeValue()
      {
         long nanoTime = observableNanoTime.get();
         long hours = TimeUnit.NANOSECONDS.toHours(nanoTime);
         long minutes = TimeUnit.NANOSECONDS.toMinutes(nanoTime);
         long seconds = TimeUnit.NANOSECONDS.toSeconds(nanoTime);
         long millis = TimeUnit.NANOSECONDS.toMillis(nanoTime);
         millis -= 1000 * seconds;
         seconds -= 60 * minutes;
         minutes -= 60 * hours;

         String time = String.format("%02ds%03d", seconds, millis);

         if (minutes > 0 || hours > 0)
            time = String.format("%02d", minutes) + "m\n" + time;
         if (hours > 0)
            time = hours + "h" + time;

         return time;
      }
   }
}
