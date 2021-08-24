package us.ihmc.scs2.session;

import us.ihmc.messager.MessagerAPIFactory;
import us.ihmc.messager.MessagerAPIFactory.Category;
import us.ihmc.messager.MessagerAPIFactory.CategoryTheme;
import us.ihmc.messager.MessagerAPIFactory.MessagerAPI;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.messager.MessagerAPIFactory.TopicTheme;

public class SessionMessagerAPI
{
   private SessionMessagerAPI()
   {
   }

   private static final MessagerAPIFactory apiFactory = new MessagerAPIFactory();

   private static final Category root = apiFactory.createRootCategory("SessionAPI");

   private static final CategoryTheme Session = apiFactory.createCategoryTheme("Session");
   private static final CategoryTheme Record = apiFactory.createCategoryTheme("Record");

   private static final CategoryTheme Run = apiFactory.createCategoryTheme("Run");
   private static final CategoryTheme Playback = apiFactory.createCategoryTheme("Playback");

   private static final TopicTheme RealTimeRate = apiFactory.createTopicTheme("RealTimeRate");
   private static final TopicTheme Period = apiFactory.createTypedTopicTheme("Period");
   private static final TopicTheme State = apiFactory.createTypedTopicTheme("State");
   private static final TopicTheme Mode = apiFactory.createTypedTopicTheme("Mode");
   private static final TopicTheme TickPeriod = apiFactory.createTypedTopicTheme("TickPeriod");

   public static final Topic<Long> SessionDTNanoseconds = root.child(Session).child(Run).topic(Period);
   public static final Topic<SessionState> SessionCurrentState = root.child(Session).topic(State);
   public static final Topic<SessionMode> SessionCurrentMode = root.child(Session).topic(Mode);
   public static final Topic<Integer> BufferRecordTickPeriod = root.child(Session).child(Record).topic(TickPeriod);

   public static final Topic<Boolean> RunAtRealTimeRate = root.child(Session).child(Run).topic(RealTimeRate);
   public static final Topic<Double> PlaybackRealTimeRate = root.child(Session).child(Playback).topic(RealTimeRate);

   public static final MessagerAPI API = apiFactory.getAPIAndCloseFactory();
}
