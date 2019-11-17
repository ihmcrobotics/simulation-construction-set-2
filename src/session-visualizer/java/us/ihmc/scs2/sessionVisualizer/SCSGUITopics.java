package us.ihmc.scs2.sessionVisualizer;

import java.io.File;

import javafx.stage.Window;
import javafx.util.Pair;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.scs2.session.SessionMessagerAPI;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.session.YoSharedBufferMessagerAPI;
import us.ihmc.scs2.sessionVisualizer.charts.ChartIntegerBounds;
import us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.search.SearchEngines;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoGroupFX;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class SCSGUITopics
{
   // GUI internal topics:
   private Topic<Object> takeSnapshot;
   private Topic<Object> registerRecordable;
   private Topic<Object> forgetRecordable;
   private Topic<Boolean> showAdvancedControls;
   private Topic<Boolean> showOverheadPlotter;

   private Topic<Object> toggleKeyFrame, requestCurrentKeyFrames;
   private Topic<Object> goToNextKeyFrame, goToPreviousKeyFrame;
   private Topic<int[]> currentKeyFrames;

   private Topic<SearchEngines> yoSearchEngine;
   private Topic<String> yoSearchTarget;
   private Topic<Integer> yoSearchMaxListSize;
   private Topic<File> yoCompositePatternLoadRequest;
   private Topic<File> yoCompositePatternSaveRequest;

   private Topic<Boolean> yoGraphicRootGroupRequest;
   private Topic<YoGroupFX> yoGraphicRootGroupData;
   private Topic<File> yoGraphicLoadRequest;
   private Topic<File> yoGraphicSaveRequest;

   private Topic<Double> yoChartZoomFactor;
   private Topic<Boolean> yoChartRequestZoomIn, yoChartRequestZoomOut;
   private Topic<Integer> yoChartRequestShift;

   private Topic<Boolean> yoChartRequestCurrentBounds;
   private Topic<ChartIntegerBounds> yoChartCurrentBounds;
   private Topic<Pair<Window, File>> yoChartGroupSaveConfiguration;
   private Topic<Pair<Window, File>> yoChartGroupLoadConfiguration;

   // Session topics
   private Topic<SessionState> sessionCurrentState;
   private Topic<SessionMode> sessionCurrentMode;
   private Topic<Boolean> runAtRealTimeRate;
   private Topic<Long> sessionTickToTimeIncrement;
   private Topic<Double> playbackRealTimeRate;
   private Topic<Boolean> remoteSessionControlsRequest;
   private Topic<Boolean> logSessionControlsRequest;

   private Topic<Integer> yoBufferCurrentIndexRequest;
   private Topic<Integer> yoBufferIncrementCurrentIndexRequest, yoBufferDecrementCurrentIndexRequest;
   private Topic<Integer> yoBufferInPointIndexRequest, yoBufferOutPointIndexRequest;
   private Topic<CropBufferRequest> yoBufferCropRequest;
   private Topic<Integer> yoBufferCurrentSizeRequest;
   private Topic<YoBufferPropertiesReadOnly> yoBufferCurrentProperties;

   public void setupTopics()
   {
      takeSnapshot = SessionVisualizerMessagerAPI.TakeSnapshot;
      registerRecordable = SessionVisualizerMessagerAPI.RegisterRecordable;
      forgetRecordable = SessionVisualizerMessagerAPI.ForgetRecordable;
      showAdvancedControls = SessionVisualizerMessagerAPI.ShowAdvancedControls;
      showOverheadPlotter = SessionVisualizerMessagerAPI.ShowOverheadPlotter;

      toggleKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.ToggleKeyFrame;
      requestCurrentKeyFrames = SessionVisualizerMessagerAPI.KeyFrame.RequestCurrentKeyFrames;
      goToNextKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.GoToNextKeyFrame;
      goToPreviousKeyFrame = SessionVisualizerMessagerAPI.KeyFrame.GoToPreviousKeyFrame;
      currentKeyFrames = SessionVisualizerMessagerAPI.KeyFrame.CurrentKeyFrames;

      yoSearchEngine = SessionVisualizerMessagerAPI.YoSearch.YoSearchEngine;
      yoSearchTarget = SessionVisualizerMessagerAPI.YoSearch.YoSearchTarget;
      yoSearchMaxListSize = SessionVisualizerMessagerAPI.YoSearch.YoSearchMaxListSize;
      yoCompositePatternLoadRequest = SessionVisualizerMessagerAPI.YoSearch.YoCompositePatternLoadRequest;
      yoCompositePatternSaveRequest = SessionVisualizerMessagerAPI.YoSearch.YoCompositePatternSaveRequest;

      yoGraphicRootGroupRequest = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicRootGroupRequest;
      yoGraphicRootGroupData = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicRootGroupData;
      yoGraphicLoadRequest = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicLoadRequest;
      yoGraphicSaveRequest = SessionVisualizerMessagerAPI.YoGraphic.YoGraphicSaveRequest;

      yoChartZoomFactor = SessionVisualizerMessagerAPI.YoChart.YoChartZoomFactor;
      yoChartRequestZoomIn = SessionVisualizerMessagerAPI.YoChart.YoChartRequestZoomIn;
      yoChartRequestZoomOut = SessionVisualizerMessagerAPI.YoChart.YoChartRequestZoomOut;
      yoChartRequestShift = SessionVisualizerMessagerAPI.YoChart.YoChartRequestShift;
      yoChartGroupSaveConfiguration = SessionVisualizerMessagerAPI.YoChart.YoChartGroupSaveConfiguration;
      yoChartGroupLoadConfiguration = SessionVisualizerMessagerAPI.YoChart.YoChartGroupLoadConfiguration;

      yoChartRequestCurrentBounds = SessionVisualizerMessagerAPI.YoChart.YoChartRequestCurrentBounds;
      yoChartCurrentBounds = SessionVisualizerMessagerAPI.YoChart.YoChartCurrentBounds;

      sessionCurrentState = SessionMessagerAPI.SessionCurrentState;
      sessionCurrentMode = SessionMessagerAPI.SessionCurrentMode;
      runAtRealTimeRate = SessionMessagerAPI.RunAtRealTimeRate;
      sessionTickToTimeIncrement = SessionMessagerAPI.SessionTickToTimeIncrement;
      playbackRealTimeRate = SessionMessagerAPI.PlaybackRealTimeRate;
      remoteSessionControlsRequest = SessionVisualizerMessagerAPI.Session.RemoteSessionControlsRequest;
      logSessionControlsRequest = SessionVisualizerMessagerAPI.Session.LogSessionControlsRequest;

      yoBufferCurrentIndexRequest = YoSharedBufferMessagerAPI.CurrentIndexRequest;
      yoBufferIncrementCurrentIndexRequest = YoSharedBufferMessagerAPI.IncrementCurrentIndexRequest;
      yoBufferDecrementCurrentIndexRequest = YoSharedBufferMessagerAPI.DecrementCurrentIndexRequest;
      yoBufferInPointIndexRequest = YoSharedBufferMessagerAPI.InPointIndexRequest;
      yoBufferOutPointIndexRequest = YoSharedBufferMessagerAPI.OutPointIndexRequest;
      yoBufferCropRequest = YoSharedBufferMessagerAPI.CropRequest;
      yoBufferCurrentSizeRequest = YoSharedBufferMessagerAPI.CurrentBufferSizeRequest;
      yoBufferCurrentProperties = YoSharedBufferMessagerAPI.CurrentBufferProperties;
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

   public Topic<String> getYoSearchTarget()
   {
      return yoSearchTarget;
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

   public Topic<Boolean> getYoGraphicRootGroupRequest()
   {
      return yoGraphicRootGroupRequest;
   }

   public Topic<YoGroupFX> getYoGraphicRootGroupData()
   {
      return yoGraphicRootGroupData;
   }

   public Topic<File> getYoGraphicLoadRequest()
   {
      return yoGraphicLoadRequest;
   }

   public Topic<File> getYoGraphicSaveRequest()
   {
      return yoGraphicSaveRequest;
   }

   public Topic<Double> getYoChartZoomFactor()
   {
      return yoChartZoomFactor;
   }

   public Topic<Boolean> getYoChartRequestZoomIn()
   {
      return yoChartRequestZoomIn;
   }

   public Topic<Boolean> getYoChartRequestZoomOut()
   {
      return yoChartRequestZoomOut;
   }

   public Topic<Integer> getYoChartRequestShift()
   {
      return yoChartRequestShift;
   }

   public Topic<Boolean> getYoChartRequestCurrentBounds()
   {
      return yoChartRequestCurrentBounds;
   }

   public Topic<ChartIntegerBounds> getYoChartCurrentBounds()
   {
      return yoChartCurrentBounds;
   }

   public Topic<Pair<Window, File>> getYoChartGroupLoadConfiguration()
   {
      return yoChartGroupLoadConfiguration;
   }

   public Topic<Pair<Window, File>> getYoChartGroupSaveConfiguration()
   {
      return yoChartGroupSaveConfiguration;
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

   public Topic<Integer> getYoBufferCurrentSizeRequest()
   {
      return yoBufferCurrentSizeRequest;
   }

   public Topic<YoBufferPropertiesReadOnly> getYoBufferCurrentProperties()
   {
      return yoBufferCurrentProperties;
   }
}
