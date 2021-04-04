package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.File;
import java.util.List;

import javafx.stage.Window;
import javafx.util.Pair;
import us.ihmc.messager.MessagerAPIFactory;
import us.ihmc.messager.MessagerAPIFactory.Category;
import us.ihmc.messager.MessagerAPIFactory.CategoryTheme;
import us.ihmc.messager.MessagerAPIFactory.MessagerAPI;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.messager.MessagerAPIFactory.TopicTheme;
import us.ihmc.messager.MessagerAPIFactory.TypedTopicTheme;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.search.SearchEngines;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class SessionVisualizerMessagerAPI
{
   private static final MessagerAPIFactory apiFactory = new MessagerAPIFactory();

   private static final Category APIRoot = apiFactory.createRootCategory("SessionVisualizer");
   private static final CategoryTheme Register = apiFactory.createCategoryTheme("Register");
   private static final CategoryTheme Forget = apiFactory.createCategoryTheme("Forget");
   private static final CategoryTheme Controls = apiFactory.createCategoryTheme("Controls");
   private static final CategoryTheme Advanced = apiFactory.createCategoryTheme("Advanced");
   private static final CategoryTheme OverheadPlotter = apiFactory.createCategoryTheme("OverheadPlotter");
   private static final CategoryTheme Group = apiFactory.createCategoryTheme("Group");
   private static final CategoryTheme Configuration = apiFactory.createCategoryTheme("Configuration");

   private static final TopicTheme Toggle = apiFactory.createTopicTheme("Toggle");
   private static final TopicTheme Next = apiFactory.createTopicTheme("Next");
   private static final TopicTheme Previous = apiFactory.createTopicTheme("Previous");
   private static final TopicTheme Snapshot = apiFactory.createTopicTheme("Snapshot");
   private static final TopicTheme Recordable = apiFactory.createTypedTopicTheme("Recordable");
   private static final TopicTheme Data = apiFactory.createTypedTopicTheme("Data");
   private static final TopicTheme Request = apiFactory.createTopicTheme("Request");
   private static final TypedTopicTheme<Integer> Size = apiFactory.createTypedTopicTheme("Size");
   private static final TypedTopicTheme<Boolean> Show = apiFactory.createTypedTopicTheme("Show");
   private static final TopicTheme Load = apiFactory.createTopicTheme("load");
   private static final TopicTheme Save = apiFactory.createTopicTheme("save");
   private static final TopicTheme Close = apiFactory.createTopicTheme("close");
   private static final TopicTheme Open = apiFactory.createTopicTheme("open");

   public static final Topic<Object> TakeSnapshot = APIRoot.topic(Snapshot);
   public static final Topic<Object> RegisterRecordable = APIRoot.child(Register).topic(Recordable);
   public static final Topic<Object> ForgetRecordable = APIRoot.child(Forget).topic(Recordable);
   public static final Topic<Boolean> ShowAdvancedControls = APIRoot.child(Controls).child(Advanced).topic(Show);
   public static final Topic<Boolean> ShowOverheadPlotter = APIRoot.child(OverheadPlotter).topic(Show);
   public static final Topic<String> OpenWindowRequest = APIRoot.topic(Open);
   public static final Topic<Boolean> SessionVisualizerCloseRequest = APIRoot.topic(Close);

   static
   { // Ensure that the KeyFrame is loaded before closing the API.
      new KeyFrame();
      new YoSearch();
      new YoGraphic();
      new YoChart();
      new YoSliderboard();
      new Session();
   }

   public static class KeyFrame
   {
      private static final CategoryTheme KeyFrame = apiFactory.createCategoryTheme("KeyFrame");

      private static final TopicTheme Current = apiFactory.createTopicTheme("Current");

      public static final Topic<Object> ToggleKeyFrame = APIRoot.child(KeyFrame).topic(Toggle);
      public static final Topic<Object> GoToNextKeyFrame = APIRoot.child(KeyFrame).topic(Next);
      public static final Topic<Object> GoToPreviousKeyFrame = APIRoot.child(KeyFrame).topic(Previous);
      public static final Topic<Object> RequestCurrentKeyFrames = APIRoot.child(KeyFrame).topic(Request);

      public static final Topic<int[]> CurrentKeyFrames = APIRoot.child(KeyFrame).topic(Current);
   }

   public static class YoSearch
   {
      private static final CategoryTheme YoSearch = apiFactory.createCategoryTheme("YoSearch");
      private static final CategoryTheme YoCompositePattern = apiFactory.createCategoryTheme("YoCompositePattern");

      private static final TypedTopicTheme<SearchEngines> SearchEngine = apiFactory.createTypedTopicTheme("SearchEngine");
      private static final TopicTheme Selected = apiFactory.createTopicTheme("Selected");
      private static final TopicTheme Refresh = apiFactory.createTopicTheme("refresh");

      public static final Topic<SearchEngines> YoSearchEngine = APIRoot.child(YoSearch).topic(SearchEngine);
      public static final Topic<Integer> YoSearchMaxListSize = APIRoot.child(YoSearch).topic(Size);
      public static final Topic<File> YoCompositePatternLoadRequest = APIRoot.child(YoSearch).child(YoCompositePattern).topic(Load);
      public static final Topic<File> YoCompositePatternSaveRequest = APIRoot.child(YoSearch).child(YoCompositePattern).topic(Save);
      public static final Topic<List<String>> YoCompositePatternSelected = APIRoot.child(YoSearch).child(YoCompositePattern).topic(Selected);
      public static final Topic<Boolean> YoCompositeRefreshAll = APIRoot.child(YoSearch).child(YoCompositePattern).topic(Refresh);
   }

   public static class YoGraphic
   {
      private static final CategoryTheme YoGraphic = apiFactory.createCategoryTheme("YoGraphic");
      private static final CategoryTheme Root = apiFactory.createCategoryTheme("Root");

      public static final Topic<Boolean> YoGraphicRootGroupRequest = APIRoot.child(YoGraphic).child(Root).child(Group).topic(Request);
      public static final Topic<YoGroupFX> YoGraphicRootGroupData = APIRoot.child(YoGraphic).child(Root).child(Group).topic(Data);
      public static final Topic<File> YoGraphicSaveRequest = APIRoot.child(YoGraphic).topic(Save);
      public static final Topic<File> YoGraphicLoadRequest = APIRoot.child(YoGraphic).topic(Load);
   }

   public static class YoChart
   {
      private static final CategoryTheme YoChart = apiFactory.createCategoryTheme("YoChart");
      private static final CategoryTheme Zoom = apiFactory.createCategoryTheme("Zoom");
      private static final CategoryTheme In = apiFactory.createCategoryTheme("In");
      private static final CategoryTheme Out = apiFactory.createCategoryTheme("Out");
      private static final CategoryTheme Shift = apiFactory.createCategoryTheme("Shift");

      private static final TopicTheme Factor = apiFactory.createTopicTheme("Factor");

      public static final Topic<Pair<Window, Double>> YoChartZoomFactor = APIRoot.child(YoChart).child(Zoom).topic(Factor);
      public static final Topic<Pair<Window, Boolean>> YoChartRequestZoomIn = APIRoot.child(YoChart).child(Zoom).child(In).topic(Request);
      public static final Topic<Pair<Window, Boolean>> YoChartRequestZoomOut = APIRoot.child(YoChart).child(Zoom).child(Out).topic(Request);
      public static final Topic<Pair<Window, Integer>> YoChartRequestShift = APIRoot.child(YoChart).child(Shift).topic(Request);
      public static final Topic<Pair<Window, File>> YoChartGroupSaveConfiguration = APIRoot.child(YoChart).child(Group).child(Configuration).topic(Save);
      public static final Topic<Pair<Window, File>> YoChartGroupLoadConfiguration = APIRoot.child(YoChart).child(Group).child(Configuration).topic(Load);
   }

   public static class YoSliderboard
   {
      private static final CategoryTheme YoSliderboard = apiFactory.createCategoryTheme("YoSliderboard");

      public static final Topic<File> YoSliderboardSaveConfiguration = APIRoot.child(YoSliderboard).child(Configuration).topic(Save);
      public static final Topic<File> YoSliderboardLoadConfiguration = APIRoot.child(YoSliderboard).child(Configuration).topic(Load);
   }

   public static class Session
   {
      private static final CategoryTheme Session = apiFactory.createCategoryTheme("Session");
      private static final CategoryTheme Remote = apiFactory.createCategoryTheme("Remote");
      private static final CategoryTheme Log = apiFactory.createCategoryTheme("Log");

      public static final Topic<Boolean> RemoteSessionControlsRequest = APIRoot.child(Session).child(Remote).child(Controls).topic(Request);
      public static final Topic<Boolean> LogSessionControlsRequest = APIRoot.child(Session).child(Log).child(Controls).topic(Request);
   }

   public static final MessagerAPI API = apiFactory.getAPIAndCloseFactory();
}
