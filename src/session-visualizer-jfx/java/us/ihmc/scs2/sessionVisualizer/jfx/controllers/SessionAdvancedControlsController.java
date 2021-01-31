package us.ihmc.scs2.sessionVisualizer.jfx.controllers;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jfoenix.controls.JFXButton;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class SessionAdvancedControlsController
{
   public static final String INACTIVE_MODE = "session-controls-inactive-mode";
   public static final String ACTIVE_MODE = "session-controls-active-mode";

   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   @FXML
   private VBox mainPane;
   @FXML
   private FlowPane buttonsContainer;
   @FXML
   private JFXButton previousKeyFrameButton, nextKeyFrameButton;
   @FXML
   private Slider bufferIndexSlider;
   @FXML
   private FontAwesomeIconView runningIconView, playbackIconView, pauseIconView;

   private Property<YoBufferPropertiesReadOnly> bufferProperties;

   private BooleanProperty showProperty = new SimpleBooleanProperty(this, "show", false);

   public SessionAdvancedControlsController()
   {
   }

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
      topics = toolkit.getTopics();

      MutableBoolean updatingIndex = new MutableBoolean(false);
      MutableBoolean updatingSlider = new MutableBoolean(false);
      bufferIndexSlider.valueProperty().addListener((o, oldValue, newValue) ->
      {
         if (updatingSlider.isFalse())
         {
            updatingIndex.setTrue();
            messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), newValue.intValue());
            updatingIndex.setFalse();
         }
      });
      messager.registerJavaFXSyncedTopicListener(topics.getYoBufferCurrentProperties(), bufferProperties ->
      {
         bufferIndexSlider.setMin(0);
         bufferIndexSlider.setMax(bufferProperties.getSize());
         if (updatingIndex.isFalse())
         {
            updatingSlider.setTrue();
            bufferIndexSlider.setValue(bufferProperties.getCurrentIndex());
            updatingSlider.setFalse();
         }
      });
      bufferProperties = messager.createPropertyInput(topics.getYoBufferCurrentProperties());

      messager.registerJavaFXSyncedTopicListener(topics.getShowAdvancedControls(), show -> showProperty.set(show));

      showProperty.addListener((o, oldValue, newValue) -> show(newValue));
      show(showProperty.get());

      ReadOnlyObjectProperty<int[]> keyFrameIndicesProperty = toolkit.getKeyFrameManager().keyFrameIndicesProperty();
      keyFrameIndicesProperty.addListener((o, oldValue, newValue) ->
      {
         boolean disableKeyFrameButtons = newValue == null || newValue.length == 0;
         previousKeyFrameButton.setDisable(disableKeyFrameButtons);
         nextKeyFrameButton.setDisable(disableKeyFrameButtons);
      });
      boolean disableKeyFrameButtons = keyFrameIndicesProperty.get() == null || keyFrameIndicesProperty.get().length == 0;
      previousKeyFrameButton.setDisable(disableKeyFrameButtons);
      nextKeyFrameButton.setDisable(disableKeyFrameButtons);

      setupMainControlsActiveMode(this, messager, topics, runningIconView, playbackIconView, pauseIconView);
   }

   public static void setupMainControlsActiveMode(Object bean, JavaFXMessager messager, SessionVisualizerTopics topics, FontAwesomeIconView runningIconView,
                                                  FontAwesomeIconView playbackIconView, FontAwesomeIconView pauseIconView)
   {
      Property<SessionState> sessionCurrentStateProperty = messager.createPropertyInput(topics.getSessionCurrentState(), null);
      Property<SessionMode> sessionCurrentModeProperty = messager.createPropertyInput(topics.getSessionCurrentMode(), null);

      BooleanProperty isRunningActive = new SimpleBooleanProperty(runningIconView, "isRunningActive", false);
      BooleanProperty isPlaybackActive = new SimpleBooleanProperty(runningIconView, "isPlaybackActive", false);
      BooleanProperty isPauseActive = new SimpleBooleanProperty(runningIconView, "isPauseActive", false);

      sessionCurrentStateProperty.addListener((o, oldValue, newValue) ->
      {
         if (newValue == SessionState.INACTIVE)
         {
            isRunningActive.set(false);
            isPlaybackActive.set(false);
            isPauseActive.set(false);
         }
      });

      ChangeListener<SessionMode> listener = (o, oldValue, newValue) ->
      {
         boolean isSessionActive = sessionCurrentStateProperty.getValue() == SessionState.ACTIVE;
         isRunningActive.set(isSessionActive && newValue == SessionMode.RUNNING);
         isPlaybackActive.set(isSessionActive && newValue == SessionMode.PLAYBACK);
         isPauseActive.set(isSessionActive && newValue == SessionMode.PAUSE);
      };
      sessionCurrentModeProperty.addListener(listener);
      listener.changed(null, null, sessionCurrentModeProperty.getValue());

      setupActiveMode(isRunningActive, runningIconView, ACTIVE_MODE, INACTIVE_MODE);
      setupActiveMode(isPlaybackActive, playbackIconView, ACTIVE_MODE, INACTIVE_MODE);
      setupActiveMode(isPauseActive, pauseIconView, ACTIVE_MODE, INACTIVE_MODE);
   }

   public static void setupActiveMode(ObservableBooleanValue observableActive, FontAwesomeIconView iconView, String activeStyleClass, String inactiveStyleClass)
   {
      InvalidationListener listener = observable ->
      {
         if (observableActive.get())
         {
            iconView.getStyleClass().remove(inactiveStyleClass);
            iconView.getStyleClass().add(activeStyleClass);
         }
         else
         {
            iconView.getStyleClass().remove(activeStyleClass);
            iconView.getStyleClass().add(inactiveStyleClass);
         }
      };
      observableActive.addListener(listener);
      listener.invalidated(observableActive);
   }

   public BooleanProperty showProperty()
   {
      return showProperty;
   }

   public void show(boolean show)
   {
      ObservableList<Node> children = mainPane.getChildren();

      if (show)
      {
         if (!children.contains(buttonsContainer))
            children.add(0, buttonsContainer);
      }
      else
      {
         children.remove(buttonsContainer);
      }
   }

   @FXML
   private void startRunning()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.RUNNING);
   }

   @FXML
   private void startPlayback()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PLAYBACK);
   }

   @FXML
   private void pause()
   {
      messager.submitMessage(topics.getSessionCurrentMode(), SessionMode.PAUSE);
   }

   @FXML
   private void setInPoint()
   {
      messager.submitMessage(topics.getYoBufferInPointIndexRequest(), bufferIndexSlider.valueProperty().getValue().intValue());
   }

   @FXML
   private void gotoInPoint()
   {
      if (bufferProperties.getValue() != null)
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), bufferProperties.getValue().getInPoint());
   }

   @FXML
   private void stepBack()
   {
      messager.submitMessage(topics.getYoBufferDecrementCurrentIndexRequest(), 1);
   }

   @FXML
   private void stepForward()
   {
      messager.submitMessage(topics.getYoBufferIncrementCurrentIndexRequest(), 1);
   }

   @FXML
   private void gotoOutPoint()
   {
      if (bufferProperties.getValue() != null)
         messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), bufferProperties.getValue().getOutPoint());
   }

   @FXML
   private void setOutPoint()
   {
      messager.submitMessage(topics.getYoBufferOutPointIndexRequest(), bufferIndexSlider.valueProperty().getValue().intValue());
   }

   @FXML
   private void gotoPreviousKeyFrame()
   {
      messager.submitMessage(topics.getGoToPreviousKeyFrame(), new Object());
   }

   @FXML
   private void addRemoveKeyFrame()
   {
      messager.submitMessage(topics.getToggleKeyFrame(), new Object());
   }

   @FXML
   private void gotoNextKeyFrame()
   {
      messager.submitMessage(topics.getGoToNextKeyFrame(), new Object());
   }

   @FXML
   private void requestZoomInGraphs()
   {
      messager.submitMessage(topics.getYoChartRequestZoomIn(), true);
   }

   @FXML
   private void requestZoomOutGraphs()
   {
      messager.submitMessage(topics.getYoChartRequestZoomOut(), true);
   }

   @FXML
   private void openSimpleControls()
   {
      messager.submitMessage(topics.getShowAdvancedControls(), false);
   }
}
