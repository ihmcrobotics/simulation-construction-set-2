package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.File;
import java.util.List;

import javafx.stage.Window;
import javafx.util.Pair;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.scs2.session.SessionMessagerAPI;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.session.YoSharedBufferMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.search.SearchEngines;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.FillBufferRequest;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class SessionVisualizerTopics
{
   // GUI internal topics:
   private Topic<Boolean> disableUserControls;
   private Topic<SceneVideoRecordingRequest> sceneVideoRecordingRequest;
   private Topic<CameraObjectTrackingRequest> cameraTrackObject;
   private Topic<Object> takeSnapshot;
   private Topic<Object> registerRecordable;
   private Topic<Object> forgetRecordable;
   private Topic<Boolean> showAdvancedControls;
   private Topic<Boolean> showOverheadPlotter;
   private Topic<Pair<String, Object>> openWindowRequest;
   private Topic<Boolean> sessionVisualizerCloseRequest;

   private Topic<Object> toggleKeyFrame, requestCurrentKeyFrames;
   private Topic<Object> goToNextKeyFrame, goToPreviousKeyFrame;
   private Topic<int[]> currentKeyFrames;

   private Topic<SearchEngines> yoSearchEngine;
   private Topic<Integer> yoSearchMaxListSize;
   private Topic<File> yoCompositePatternLoadRequest;
   private Topic<File> yoCompositePatternSaveRequest;
   private Topic<List<String>> yoCompositeSelected;
   private Topic<Boolean> yoCompositeRefreshAll;

   private Topic<File> yoGraphicLoadRequest;
   private Topic<File> yoGraphicSaveRequest;

   private Topic<Pair<Window, Double>> yoChartZoomFactor;
   private Topic<Pair<Window, Boolean>> yoChartRequestZoomIn, yoChartRequestZoomOut;
   private Topic<Pair<Window, Integer>> yoChartRequestShift;

   private Topic<Pair<Window, File>> yoChartGroupSaveConfiguration;
   private Topic<Pair<Window, File>> yoChartGroupLoadConfiguration;
   private Topic<Pair<Window, String>> yoChartGroupName;

   private Topic<File> yoSliderboardSaveConfiguration;
   private Topic<File> yoSliderboardLoadConfiguration;

   private Topic<Integer> controlsNumberPrecision;

   private Topic<File> sessionVisualizerConfigurationLoadRequest;
   private Topic<File> sessionVisualizerConfigurationSaveRequest;
   private Topic<Boolean> sessionVisualizerDefaultConfigurationLoadRequest;
   private Topic<Boolean> sessionVisualizerDefaultConfigurationSaveRequest;

   // Session topics
   private Topic<SessionState> sessionCurrentState;
   private Topic<SessionMode> sessionCurrentMode;
   private Topic<Boolean> runAtRealTimeRate;
   private Topic<Long> sessionTickToTimeIncrement;
   private Topic<Double> playbackRealTimeRate;
   private Topic<Integer> bufferRecordTickPeriod;
   private Topic<Boolean> remoteSessionControlsRequest;
   private Topic<Boolean> logSessionControlsRequest;

   private Topic<Integer> yoBufferCurrentIndexRequest;
   private Topic<Integer> yoBufferIncrementCurrentIndexRequest, yoBufferDecrementCurrentIndexRequest;
   private Topic<Integer> yoBufferInPointIndexRequest, yoBufferOutPointIndexRequest;
   private Topic<CropBufferRequest> yoBufferCropRequest;
   private Topic<FillBufferRequest> yoBufferFillRequest;
   private Topic<Integer> yoBufferCurrentSizeRequest;
   private Topic<YoBufferPropertiesReadOnly> yoBufferCurrentProperties;

   public void setupTopics()
   {
      disableUserControls = SessionVisualizerMessagerAPI.DisableUserControls;
      sceneVideoRecordingRequest = SessionVisualizerMessagerAPI.SceneVideoRecordingRequest;
      cameraTrackObject = SessionVisualizerMessagerAPI.CameraTrackObject;
      takeSnapshot = SessionVisualizerMessagerAPI.TakeSnapshot;
      registerRecordable = SessionVisualizerMessagerAPI.RegisterRecordable;
      forgetRecordable = SessionVisualizerMessagerAPI.ForgetRecordable;
      showAdvancedControls = SessionVisualizerMessagerAPI.ShowAdvancedControls;
      showOverheadPlotter = SessionVisualizerMessagerAPI.ShowOverheadPlotter;
      openWindowRequest = SessionVisualizerMessagerAPI.OpenWindowRequest;
      sessionVisualizerCloseRequest = SessionVisualizerMessagerAPI.SessionVisualizerCloseRequest;

      toggleKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.ToggleKeyFrame;
      requestCurrentKeyFrames = SessionVisualizerMessagerAPI.KeyFrame.RequestCurrentKeyFrames;
      goToNextKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.GoToNextKeyFrame;
      goToPreviousKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.GoToPreviousKeyFrame;
      currentKeyFrames = SessionVisualizerMessagerAPI.KeyFrame.CurrentKeyFrames;

      yoSearchEngine = SessionVisualizerMessagerAPI.YoSearch.YoSearchEngine;
      yoSearchMaxListSize = SessionVisualizerMessagerAPI.YoSearch.YoSearchMaxListSize;
      yoCompositePatternLoadRequest = SessionVisualizerMessagerAPI.YoSearch.YoCompositePatternLoadRequest;
      yoCompositePatternSaveRequest = SessionVisualizerMessagerAPI.YoSearch.YoCompositePatternSaveRequest;
      yoCompositeSelected = SessionVisualizerMessagerAPI.YoSearch.YoCompositePatternSelected;
      yoCompositeRefreshAll = SessionVisualizerMessagerAPI.YoSearch.YoCompositeRefreshAll;

      yoGraphicLoadRequest = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicLoadRequest;
      yoGraphicSaveRequest = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicSaveRequest;

      yoChartZoomFactor = SessionVisualizerMessagerAPI.YoChart.YoChartZoomFactor;
      yoChartRequestZoomIn = SessionVisualizerMessagerAPI.YoChart.YoChartRequestZoomIn;
      yoChartRequestZoomOut = SessionVisualizerMessagerAPI.YoChart.YoChartRequestZoomOut;
      yoChartRequestShift = SessionVisualizerMessagerAPI.YoChart.YoChartRequestShift;
      yoChartGroupSaveConfiguration = SessionVisualizerMessagerAPI.YoChart.YoChartGroupSaveConfiguration;
      yoChartGroupLoadConfiguration = SessionVisualizerMessagerAPI.YoChart.YoChartGroupLoadConfiguration;
      yoChartGroupName = SessionVisualizerMessagerAPI.YoChart.YoChartGroupName;

      yoSliderboardSaveConfiguration = SessionVisualizerMessagerAPI.YoSliderboard.YoSliderboardSaveConfiguration;
      yoSliderboardLoadConfiguration = SessionVisualizerMessagerAPI.YoSliderboard.YoSliderboardLoadConfiguration;

      controlsNumberPrecision = SessionVisualizerMessagerAPI.ControlsNumberPrecision;

      sessionVisualizerConfigurationLoadRequest = SessionVisualizerMessagerAPI.SessionVisualizerConfigurationLoadRequest;
      sessionVisualizerConfigurationSaveRequest = SessionVisualizerMessagerAPI.SessionVisualizerConfigurationSaveRequest;
      sessionVisualizerDefaultConfigurationLoadRequest = SessionVisualizerMessagerAPI.SessionVisualizerDefaultConfigurationLoadRequest;
      sessionVisualizerDefaultConfigurationSaveRequest = SessionVisualizerMessagerAPI.SessionVisualizerDefaultConfigurationSaveRequest;

      sessionCurrentState = SessionMessagerAPI.SessionCurrentState;
      sessionCurrentMode = SessionMessagerAPI.SessionCurrentMode;
      runAtRealTimeRate = SessionMessagerAPI.RunAtRealTimeRate;
      sessionTickToTimeIncrement = SessionMessagerAPI.SessionTickToTimeIncrement;
      playbackRealTimeRate = SessionMessagerAPI.PlaybackRealTimeRate;
      bufferRecordTickPeriod = SessionMessagerAPI.BufferRecordTickPeriod;
      remoteSessionControlsRequest = SessionVisualizerMessagerAPI.Session.RemoteSessionControlsRequest;
      logSessionControlsRequest = SessionVisualizerMessagerAPI.Session.LogSessionControlsRequest;

      yoBufferCurrentIndexRequest = YoSharedBufferMessagerAPI.CurrentIndexRequest;
      yoBufferIncrementCurrentIndexRequest = YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest;
      yoBufferDecrementCurrentIndexRequest = YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest;
      yoBufferInPointIndexRequest = YoSharedBufferMessagerAPI.InPointIndexRequest;
      yoBufferOutPointIndexRequest = YoSharedBufferMessagerAPI.OutPointIndexRequest;
      yoBufferCropRequest = YoSharedBufferMessagerAPI.CropRequest;
      yoBufferFillRequest = YoSharedBufferMessagerAPI.FillRequest;
      yoBufferCurrentSizeRequest = YoSharedBufferMessagerAPI.CurrentBufferSizeRequest;
      yoBufferCurrentProperties = YoSharedBufferMessagerAPI.CurrentBufferProperties;
   }

   public Topic<Boolean> getDisableUserControls()
   {
      return disableUserControls;
   }

   public Topic<SceneVideoRecordingRequest> getSceneVideoRecordingRequest()
   {
      return sceneVideoRecordingRequest;
   }

   public Topic<CameraObjectTrackingRequest> getCameraTrackObject()
   {
      return cameraTrackObject;
   }

   public Topic<Object> getTakeSnapshot()
   {
      return takeSnapshot;
   }

   public Topic<Object> getRegisterRecordable()
   {
      return registerRecordable;
   }

   public Topic<Object> getForgetRecordable()
   {
      return forgetRecordable;
   }

   public Topic<Boolean> getShowAdvancedControls()
   {
      return showAdvancedControls;
   }

   public Topic<Boolean> getShowOverheadPlotter()
   {
      return showOverheadPlotter;
   }

   public Topic<Pair<String, Object>> getOpenWindowRequest()
   {
      return openWindowRequest;
   }

   public Topic<Boolean> getSessionVisualizerCloseRequest()
   {
      return sessionVisualizerCloseRequest;
   }

   public Topic<Object> getToggleKeyFrame()
   {
      return toggleKeyFrame;
   }

   public Topic<Object> getRequestCurrentKeyFrames()
   {
      return requestCurrentKeyFrames;
   }

   public Topic<Object> getGoToNextKeyFrame()
   {
      return goToNextKeyFrame;
   }

   public Topic<Object> getGoToPreviousKeyFrame()
   {
      return goToPreviousKeyFrame;
   }

   public Topic<int[]> getCurrentKeyFrames()
   {
      return currentKeyFrames;
   }

   public Topic<SearchEngines> getYoSearchEngine()
   {
      return yoSearchEngine;
   }

   public Topic<Integer> getYoSearchMaxListSize()
   {
      return yoSearchMaxListSize;
   }

   public Topic<File> getYoCompositePatternLoadRequest()
   {
      return yoCompositePatternLoadRequest;
   }

   public Topic<File> getYoCompositePatternSaveRequest()
   {
      return yoCompositePatternSaveRequest;
   }

   public Topic<List<String>> getYoCompositeSelected()
   {
      return yoCompositeSelected;
   }

   public Topic<Boolean> getYoCompositeRefreshAll()
   {
      return yoCompositeRefreshAll;
   }

   public Topic<File> getYoGraphicLoadRequest()
   {
      return yoGraphicLoadRequest;
   }

   public Topic<File> getYoGraphicSaveRequest()
   {
      return yoGraphicSaveRequest;
   }

   public Topic<Pair<Window, Double>> getYoChartZoomFactor()
   {
      return yoChartZoomFactor;
   }

   public Topic<Pair<Window, Boolean>> getYoChartRequestZoomIn()
   {
      return yoChartRequestZoomIn;
   }

   public Topic<Pair<Window, Boolean>> getYoChartRequestZoomOut()
   {
      return yoChartRequestZoomOut;
   }

   public Topic<Pair<Window, Integer>> getYoChartRequestShift()
   {
      return yoChartRequestShift;
   }

   public Topic<Pair<Window, File>> getYoChartGroupLoadConfiguration()
   {
      return yoChartGroupLoadConfiguration;
   }

   public Topic<Pair<Window, String>> getYoChartGroupName()
   {
      return yoChartGroupName;
   }

   public Topic<Pair<Window, File>> getYoChartGroupSaveConfiguration()
   {
      return yoChartGroupSaveConfiguration;
   }

   public Topic<File> getYoSliderboardLoadConfiguration()
   {
      return yoSliderboardLoadConfiguration;
   }

   public Topic<File> getYoSliderboardSaveConfiguration()
   {
      return yoSliderboardSaveConfiguration;
   }

   public Topic<Integer> getControlsNumberPrecision()
   {
      return controlsNumberPrecision;
   }

   public Topic<File> getSessionVisualizerConfigurationLoadRequest()
   {
      return sessionVisualizerConfigurationLoadRequest;
   }

   public Topic<File> getSessionVisualizerConfigurationSaveRequest()
   {
      return sessionVisualizerConfigurationSaveRequest;
   }

   public Topic<Boolean> getSessionVisualizerDefaultConfigurationLoadRequest()
   {
      return sessionVisualizerDefaultConfigurationLoadRequest;
   }

   public Topic<Boolean> getSessionVisualizerDefaultConfigurationSaveRequest()
   {
      return sessionVisualizerDefaultConfigurationSaveRequest;
   }

   public Topic<SessionState> getSessionCurrentState()
   {
      return sessionCurrentState;
   }

   public Topic<SessionMode> getSessionCurrentMode()
   {
      return sessionCurrentMode;
   }

   public Topic<Boolean> getRunAtRealTimeRate()
   {
      return runAtRealTimeRate;
   }

   public Topic<Long> getSessionTickToTimeIncrement()
   {
      return sessionTickToTimeIncrement;
   }

   public Topic<Double> getPlaybackRealTimeRate()
   {
      return playbackRealTimeRate;
   }

   public Topic<Integer> getBufferRecordTickPeriod()
   {
      return bufferRecordTickPeriod;
   }

   public Topic<Boolean> getRemoteSessionControlsRequest()
   {
      return remoteSessionControlsRequest;
   }

   public Topic<Boolean> getLogSessionControlsRequest()
   {
      return logSessionControlsRequest;
   }

   public Topic<Integer> getYoBufferCurrentIndexRequest()
   {
      return yoBufferCurrentIndexRequest;
   }

   public Topic<Integer> getYoBufferIncrementCurrentIndexRequest()
   {
      return yoBufferIncrementCurrentIndexRequest;
   }

   public Topic<Integer> getYoBufferDecrementCurrentIndexRequest()
   {
      return yoBufferDecrementCurrentIndexRequest;
   }

   public Topic<Integer> getYoBufferInPointIndexRequest()
   {
      return yoBufferInPointIndexRequest;
   }

   public Topic<Integer> getYoBufferOutPointIndexRequest()
   {
      return yoBufferOutPointIndexRequest;
   }

   public Topic<CropBufferRequest> getYoBufferCropRequest()
   {
      return yoBufferCropRequest;
   }

   public Topic<FillBufferRequest> getYoBufferFillRequest()
   {
      return yoBufferFillRequest;
   }

   public Topic<Integer> getYoBufferCurrentSizeRequest()
   {
      return yoBufferCurrentSizeRequest;
   }

   public Topic<YoBufferPropertiesReadOnly> getYoBufferCurrentProperties()
   {
      return yoBufferCurrentProperties;
   }
}
