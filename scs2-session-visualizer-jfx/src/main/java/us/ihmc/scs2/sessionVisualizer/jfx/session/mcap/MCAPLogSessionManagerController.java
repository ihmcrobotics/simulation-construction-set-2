package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTrimSlider;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import us.ihmc.log.LogTools;
import us.ihmc.messager.TopicListener;
import us.ihmc.messager.javafx.JavaFXMessager;
import us.ihmc.scs2.session.SessionRobotDefinitionListChange;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPConsoleLogItem;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPLogLevel;
import us.ihmc.scs2.session.mcap.MCAPLogFileReader;
import us.ihmc.scs2.session.mcap.MCAPLogSession;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.BackgroundExecutorManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.session.SessionControlsController;
import us.ihmc.scs2.sessionVisualizer.jfx.session.log.LogSessionManagerController.TimeStringBinding;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.PositiveIntegerValueFilter;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.simulation.SpyList;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MCAPLogSessionManagerController implements SessionControlsController
{
   private static final double THUMBNAIL_WIDTH = 200.0;

   private static final String LOG_FILE_KEY = "MCAPLogFilePath";
   private static final String ROBOT_MODEL_FILE_KEY = "MCAPRobotModelFilePath";

   private static final String DEFAULT_MODEL_TEXT_FIELD_TEXT = "Path to model file";
   private final ObjectProperty<FFMPEGMultiVideoViewer> multiVideoViewerObjectProperty = new SimpleObjectProperty<>(this, "multiVideoThumbnailViewer", null);
   private final ObjectProperty<MCAPLogSession> activeSessionProperty = new SimpleObjectProperty<>(this, "activeSession", null);
   private final LongProperty desiredLogDTProperty = new SimpleLongProperty(this, "desiredLogDT", TimeUnit.MILLISECONDS.toNanos(1));
   @FXML
   private AnchorPane mainPane;
   @FXML
   private Button openSessionButton, endSessionButton;
   @FXML
   private Label sessionNameLabel, dateLabel, logPathLabel;
   @FXML
   private JFXTrimSlider logPositionSlider;
   @FXML
   private JFXTextField currentModelFilePathTextField;
   @FXML
   private TextField desiredLogDTTextField;
   @FXML
   private TextField bufferRecordTickPeriodTextField;
   @FXML
   private TitledPane thumbnailsTitledPane;
   @FXML
   private FlowPane videoThumbnailPane;
   @FXML
   private ListView<MCAPConsoleLogItem> consoleOutputListView;
   private Stage stage;
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;
   private BackgroundExecutorManager backgroundExecutorManager;
   private File defaultRobotModelFile = null;

   private static String getDate(String filename)
   { // FIXME it seems that the timestamps in the MCAP file are epoch unix instant. Should use that.
      String year = filename.substring(0, 4);
      String month = filename.substring(4, 6);
      String day = filename.substring(6, 8);
      String hour = filename.substring(9, 11);
      String minute = filename.substring(11, 13);
      String second = filename.substring(13, 15);

      return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
   }

   @Override
   public void initialize(SessionVisualizerToolkit toolkit)
   {
      stage = new Stage();

      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
      backgroundExecutorManager = toolkit.getBackgroundExecutorManager();

      logPositionSlider.setValueFactory(param -> new TimeStringBinding(param.valueProperty(), position ->
      {
         if (activeSessionProperty.get() == null)
            return 0;
         MCAPLogFileReader mcapLogFileReader = activeSessionProperty.get().getMCAPLogFileReader();
         return mcapLogFileReader.getRelativeTimestampAtIndex(position.intValue());
      }));

      TextFormatter<Integer> recordPeriodFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, new PositiveIntegerValueFilter());
      bufferRecordTickPeriodTextField.setTextFormatter(recordPeriodFormatter);
      messager.bindBidirectional(topics.getBufferRecordTickPeriod(), recordPeriodFormatter.valueProperty(), false);

      desiredLogDTTextField.setTextFormatter(new TextFormatter<>(new LongStringConverter(), 1000L, new PositiveIntegerValueFilter()));
      MutableBoolean desiredLogDTTextFieldUpdate = new MutableBoolean(false);
      desiredLogDTTextField.textProperty().addListener((o, oldValue, newValue) ->
                                                       {
                                                          if (desiredLogDTTextFieldUpdate.isTrue())
                                                             return;
                                                          desiredLogDTTextFieldUpdate.setTrue();
                                                          try
                                                          {
                                                             desiredLogDTProperty.set(TimeUnit.MICROSECONDS.toNanos(Long.parseLong(newValue)));
                                                          }
                                                          catch (NumberFormatException e)
                                                          {
                                                             desiredLogDTTextField.setText(Long.toString(TimeUnit.NANOSECONDS.toMicros(desiredLogDTProperty.get())));
                                                          }
                                                          finally
                                                          {
                                                             desiredLogDTTextFieldUpdate.setFalse();
                                                          }
                                                       });
      desiredLogDTProperty.addListener((o, oldValue, newValue) ->
                                       {
                                          if (desiredLogDTTextFieldUpdate.isTrue())
                                             return;
                                          desiredLogDTTextFieldUpdate.setTrue();
                                          desiredLogDTTextField.setText(Long.toString(TimeUnit.NANOSECONDS.toMicros(newValue.longValue())));
                                          desiredLogDTTextFieldUpdate.setFalse();
                                       });

      ChangeListener<? super MCAPLogSession> activeSessionListener = (o, oldValue, newValue) ->
      {
         if (newValue == null)
         {
            clearControls();
         }
         else
         {
            messager.submitMessage(topics.getStartNewSessionRequest(), newValue);
            initializeControls(newValue);
         }
      };

      AtomicBoolean logPositionUpdate = new AtomicBoolean(true);

      logPositionSlider.valueProperty().addListener((o, oldValue, newValue) ->
                                                    {
                                                       MCAPLogSession logSession = activeSessionProperty.get();
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
         MCAPLogSession logSession = activeSessionProperty.get();

         if (sliderFeedbackEnabled.get())
         {
            int currentLogPosition = logSession.getMCAPLogFileReader().getCurrentIndex();

            JavaFXMissingTools.runLater(getClass(), () ->
            {
               if (logSession == null || logSession.getMCAPLogFileReader() == null)
                  return;

               if (currentLogPosition != logPositionSlider.valueProperty().intValue())
               {
                  logPositionUpdate.set(true);
                  logPositionSlider.setValue(currentLogPosition);
                  logPositionUpdate.set(false);
               }
            });
         }
      };

      activeSessionProperty.addListener((o, oldValue, newValue) ->
                                        {
                                           if (oldValue != null)
                                              oldValue.removeCurrentBufferPropertiesListener(logPositionUpdateListener);
                                           if (newValue != null)
                                           {
                                              newValue.addCurrentBufferPropertiesListener(logPositionUpdateListener);
                                              if (newValue.getInitialRobotModelFile() != null && defaultRobotModelFile == null)
                                              { // Display the robot model file path if it is available.
                                                 currentModelFilePathTextField.setText(newValue.getInitialRobotModelFile().getAbsolutePath());
                                              }
                                              else if (newValue.getInitialRobotModelFile() == null)
                                              {
                                                 currentModelFilePathTextField.setText(DEFAULT_MODEL_TEXT_FIELD_TEXT);
                                              }
                                              // Otherwise the text field will be updated when the robot model file is loaded.
                                           }
                                        });

      multiVideoViewerObjectProperty.addListener((o, oldValue, newValue) ->
                                                 {
                                                    if (oldValue != null)
                                                       oldValue.stop();
                                                    if (newValue != null)
                                                       newValue.start();
                                                 });

      if (toolkit.getSession() instanceof MCAPLogSession mcapLogSession)
      {
         desiredLogDTProperty.set(mcapLogSession.getDesiredLogDT());
         activeSessionProperty.set(mcapLogSession);
         initializeControls(mcapLogSession);
      }
      else
      {
         clearControls();
      }

      activeSessionProperty.addListener(activeSessionListener);

      thumbnailsTitledPane.expandedProperty().addListener((o, oldValue, newValue) -> JavaFXMissingTools.runLater(getClass(), stage::sizeToScene));

      openSessionButton.setOnAction(e -> openLogFile());

      endSessionButton.setOnAction(e ->
                                   {
                                      MCAPLogSession logSession = activeSessionProperty.get();
                                      if (logSession != null)
                                         logSession.shutdownSession();
                                      activeSessionProperty.set(null);
                                   });

      messager.addFXTopicListener(topics.getDisableUserControls(), m ->
      {
         openSessionButton.setDisable(m);
         endSessionButton.setDisable(m);
         logPositionSlider.setDisable(m);
      });

      stage.setScene(new Scene(mainPane));
      stage.setTitle("MCAP Log session controls");
      stage.getIcons().add(SessionVisualizerIOTools.LOG_SESSION_IMAGE);
      toolkit.getMainWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
      {
         if (!e.isConsumed())
            shutdown();
      });
   }

   private void initializeControls(MCAPLogSession session)
   {
      File logFile = session.getMCAPFile();
      MCAPLogFileReader mcapLogFileReader = session.getMCAPLogFileReader();

      sessionNameLabel.setText(session.getSessionName());
      dateLabel.setText(getDate(logFile.getName()));
      logPathLabel.setText(logFile.getAbsolutePath());
      endSessionButton.setDisable(false);
      logPositionSlider.setDisable(false);
      logPositionSlider.setValue(0.0);
      logPositionSlider.setMin(0.0);
      logPositionSlider.setMax(mcapLogFileReader.getNumberOfEntries() - 1);
      FFMPEGMultiVideoDataReader multiReader = new FFMPEGMultiVideoDataReader(logFile.getParentFile(), backgroundExecutorManager);
      multiReader.readVideoFrameNow(mcapLogFileReader.getCurrentRelativeTimestamp());
      mcapLogFileReader.getCurrentTimestamp().addListener(v -> multiReader.readVideoFrameInBackground(mcapLogFileReader.getCurrentRelativeTimestamp()));
      multiVideoViewerObjectProperty.set(new FFMPEGMultiVideoViewer(stage, videoThumbnailPane, multiReader, THUMBNAIL_WIDTH));
      boolean logHasVideos = multiReader.getNumberOfVideos() > 0;
      thumbnailsTitledPane.setText(logHasVideos ? "Logged videos" : "No video");
      thumbnailsTitledPane.setExpanded(logHasVideos);
      thumbnailsTitledPane.setDisable(!logHasVideos);

      // Setup the console output
      consoleOutputListView.setCellFactory(param -> new MCAPConsoleLogItemListCell(session.getMCAPLogFileReader().getCurrentTimestamp()));
      consoleOutputListView.getItems().clear();
      SpyList<MCAPConsoleLogItem> sessionLogItems = session.getMCAPLogFileReader().getConsoleLogManager().getAllConsoleLogItems();
      consoleOutputListView.getItems().setAll(sessionLogItems);
      sessionLogItems.addListener((change) ->
                                  {
                                     if (change.wasAdded())
                                        JavaFXMissingTools.runLater(getClass(), () -> consoleOutputListView.getItems().setAll(sessionLogItems));
                                  });

      JavaFXMissingTools.runNFramesLater(5, () -> stage.sizeToScene());
      JavaFXMissingTools.runNFramesLater(6, () -> stage.toFront());
   }

   private void clearControls()
   {
      sessionNameLabel.setText("N/D");
      dateLabel.setText("N/D");
      logPathLabel.setText("N/D");
      endSessionButton.setDisable(true);
      logPositionSlider.setDisable(true);
      multiVideoViewerObjectProperty.set(null);
      consoleOutputListView.getItems().clear();
   }

   public void openLogFile()
   {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory(SessionVisualizerIOTools.getDefaultFilePath(LOG_FILE_KEY));
      fileChooser.getExtensionFilters().add(new ExtensionFilter("MCAP Log file", "*.mcap"));
      fileChooser.setTitle("Choose MCAP log file");
      File result = fileChooser.showOpenDialog(stage);
      if (result == null)
         return;

      unloadSession();

      backgroundExecutorManager.executeInBackground(() ->
                                                    {
                                                       try
                                                       {
                                                          LogTools.info("Creating log session.");
                                                          MCAPLogSession newSession = new MCAPLogSession(result,
                                                                                                         desiredLogDTProperty.get(),
                                                                                                         defaultRobotModelFile);
                                                          LogTools.info("Created log session.");
                                                          JavaFXMissingTools.runLater(getClass(), () -> activeSessionProperty.set(newSession));
                                                          SessionVisualizerIOTools.setDefaultFilePath(LOG_FILE_KEY, result);
                                                       }
                                                       catch (Exception ex)
                                                       {
                                                          ex.printStackTrace();
                                                       }
                                                    });
   }

   @Override
   public void notifySessionLoaded()
   {
      // TODO Auto-generated method stub
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
   public void shutdown()
   {
      // TODO Auto-generated method stub

      stage.close();
   }

   @Override
   public Stage getStage()
   {
      return stage;
   }

   @FXML
   public void requestLoadRobotModelFile(ActionEvent actionEvent)
   {
      File result = SessionVisualizerIOTools.showOpenDialog(stage,
                                                            "Choose robot model file",
                                                            new ExtensionFilter("Robot model file", "*.urdf", "*.sdf"),
                                                            ROBOT_MODEL_FILE_KEY);
      if (result == null)
         return;

      backgroundExecutorManager.executeInBackground(() -> submitRobotDefinitionRequest(result));
   }

   private void submitRobotDefinitionRequest(File result)
   {
      MCAPLogSession logSession = activeSessionProperty.get();
      if (logSession == null)
      { // Save the file for the next session.
         defaultRobotModelFile = result;
         currentModelFilePathTextField.setText(result.getAbsolutePath());
         return;
      }

      boolean hasARobot = !logSession.getRobotDefinitions().isEmpty();
      SessionRobotDefinitionListChange request;
      if (hasARobot)
         request = SessionRobotDefinitionListChange.replace(result, logSession.getRobotDefinitions().get(0));
      else
         request = SessionRobotDefinitionListChange.add(result);

      messager.submitMessage(topics.getSessionRobotDefinitionListChangeRequest(), request);
      currentModelFilePathTextField.setText("Loading...");
      TopicListener<SessionRobotDefinitionListChange> listener = new TopicListener<SessionRobotDefinitionListChange>()
      {
         @Override
         public void receivedMessageForTopic(SessionRobotDefinitionListChange m)
         {
            if (m.getAddedRobotDefinition() != null)
            {
               currentModelFilePathTextField.setText(result.getAbsolutePath());
               defaultRobotModelFile = result;
            }
            else
            {
               currentModelFilePathTextField.setText("Failed to load.");
               defaultRobotModelFile = null;
            }
            messager.removeFXTopicListener(topics.getSessionRobotDefinitionListChangeState(), this);
         }
      };
      messager.addFXTopicListener(topics.getSessionRobotDefinitionListChangeState(), listener);
   }

   private static class MCAPConsoleLogItemListCell extends javafx.scene.control.ListCell<MCAPConsoleLogItem>
   {
      private final Color defaultColor = Color.BLACK;
      private final Map<MCAPLogLevel, Color> logLevelToColorMap = Map.of(MCAPLogLevel.UNKNOWN,
                                                                         defaultColor,
                                                                         MCAPLogLevel.INFO,
                                                                         Color.CORNFLOWERBLUE,
                                                                         MCAPLogLevel.WARNING,
                                                                         Color.ORANGE,
                                                                         MCAPLogLevel.ERROR,
                                                                         Color.RED,
                                                                         MCAPLogLevel.FATAL,
                                                                         Color.DARKRED);

      private final Color futureColor = Color.GRAY.deriveColor(0, 1, 1, 0.5);
      private final Map<MCAPLogLevel, String> logLevelToStringMap = Map.of(MCAPLogLevel.UNKNOWN,
                                                                           "  ???",
                                                                           MCAPLogLevel.INFO,
                                                                           " INFO",
                                                                           MCAPLogLevel.WARNING,
                                                                           " WARN",
                                                                           MCAPLogLevel.ERROR,
                                                                           "ERROR",
                                                                           MCAPLogLevel.FATAL,
                                                                           "FATAL");
      private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS a z");
      private final ZoneId zoneId = ZoneId.systemDefault(); // Need to parameterize this.
      private final YoLong currentTimestamp;
      private YoVariableChangedListener timestampListener;

      public MCAPConsoleLogItemListCell(YoLong currentTimestamp)
      {
         this.currentTimestamp = currentTimestamp;
      }

      @Override
      protected void updateItem(MCAPConsoleLogItem item, boolean empty)
      {
         super.updateItem(item, empty);

         if (empty || item == null)
         {
            setText(null);
            setGraphic(null);
            if (timestampListener != null)
               currentTimestamp.removeListener(timestampListener);
            timestampListener = null;
         }
         else
         {
            setFont(Font.font("Monospaced", 14.0));

            updateTextFill(item);
            timestampListener = v -> updateTextFill(item);
            currentTimestamp.addListener(timestampListener);
            String dateTimeFormatted = dateTimeFormatter.format(item.instant().atZone(zoneId));
            setText("[%s] [%s]\n\t[%s]: %s".formatted(logLevelToStringMap.get(item.logLevel()), dateTimeFormatted, item.processName(), item.message()));
            setGraphic(null);
         }
      }

      private void updateTextFill(MCAPConsoleLogItem item)
      {
         if (item.logTime() > currentTimestamp.getValue())
            setTextFill(futureColor);
         else if (logLevelToColorMap.containsKey(item.logLevel()))
            setTextFill(logLevelToColorMap.get(item.logLevel()));
         else
            setTextFill(logLevelToColorMap.get(MCAPLogLevel.UNKNOWN));
      }
   }

   public static void main(String[] args)
   {
      int seconds = 1701192801;
      int nanos = 789030589;
      Instant instant = Instant.ofEpochSecond(seconds, nanos);
      System.out.println(instant);
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS a z");
      System.out.println(dateTimeFormatter.format(instant.atZone(ZoneId.systemDefault())));
   }
}
