package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.referenceFrame.tools.ReferenceFrameTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;

public class ReferenceFrameManager implements Manager
{
   public static final String WORLD_FRAME = "worldFrame";

   private ReferenceFrame worldFrame = ReferenceFrameTools.constructARootFrame(WORLD_FRAME);

   private final ObjectProperty<Map<String, ReferenceFrame>> uniqueNameToReferenceFrameMapProperty = new SimpleObjectProperty<>(this,
                                                                                                                                "uniqueNameToReferenceFrameMap",
                                                                                                                                null);
   private final ObjectProperty<Map<ReferenceFrame, String>> referenceFrameToUniqueNameMapProperty = new SimpleObjectProperty<>(this,
                                                                                                                                "referenceFrameToUniqueNameMap",
                                                                                                                                null);

   private final ObjectProperty<Map<String, ReferenceFrame>> fullnameToReferenceFrameMapProperty = new SimpleObjectProperty<>(this,
                                                                                                                              "fullnameToReferenceFrame",
                                                                                                                              null);

   private final BackgroundExecutorManager backgroundExecutorManager;

   public ReferenceFrameManager(BackgroundExecutorManager backgroundExecutorManager)
   {
      this.backgroundExecutorManager = backgroundExecutorManager;
   }

   @Override
   public void startSession(Session session)
   {
      worldFrame = ReferenceFrameTools.constructARootFrame(WORLD_FRAME);
      refreshReferenceFramesNow();
   }

   @Override
   public void stopSession()
   {
      // Throw away the previous worldFrame to discard all the reference frames.
      worldFrame = null;
      uniqueNameToReferenceFrameMapProperty.set(null);
      referenceFrameToUniqueNameMapProperty.set(null);
      fullnameToReferenceFrameMapProperty.set(null);
   }

   @Override
   public boolean isSessionLoaded()
   {
      if (uniqueNameToReferenceFrameMapProperty.get() == null)
         return false;
      if (referenceFrameToUniqueNameMapProperty.get() == null)
         return false;
      if (fullnameToReferenceFrameMapProperty.get() == null)
         return false;
      return true;
   }

   public void refreshReferenceFramesNow()
   {
      Collection<ReferenceFrame> allReferenceFrames = ReferenceFrameTools.getAllFramesInTree(worldFrame);
      computeFullnameMap(allReferenceFrames);
      computeUniqueNameMaps(allReferenceFrames);
   }

   public void refreshReferenceFrames()
   {
      Collection<ReferenceFrame> allReferenceFrames = ReferenceFrameTools.getAllFramesInTree(worldFrame);
      backgroundExecutorManager.queueTaskToExecuteInBackground(this, () -> computeFullnameMap(allReferenceFrames));
      backgroundExecutorManager.queueTaskToExecuteInBackground(this, () -> computeUniqueNameMaps(allReferenceFrames));
   }

   private void computeUniqueNameMaps(Collection<ReferenceFrame> allReferenceFrames)
   {
      Map<ReferenceFrame, String> newMap = YoCompositeTools.computeUniqueNames(allReferenceFrames,
                                                                               ReferenceFrameManager::getFrameNamespace,
                                                                               ReferenceFrame::getName);
      Map<String, ReferenceFrame> newUniqueNameToReferenceFrameMap = new LinkedHashMap<>();
      Map<ReferenceFrame, String> newReferenceFrameToUniqueNameMap = new LinkedHashMap<>();

      newMap.entrySet().forEach(e ->
      {
         newUniqueNameToReferenceFrameMap.put(e.getValue(), e.getKey());
         newReferenceFrameToUniqueNameMap.put(e.getKey(), e.getValue());
      });

      JavaFXMissingTools.runLaterIfNeeded(getClass(), () ->
      {
         uniqueNameToReferenceFrameMapProperty.set(newUniqueNameToReferenceFrameMap);
         referenceFrameToUniqueNameMapProperty.set(newReferenceFrameToUniqueNameMap);
      });
   }

   private void computeFullnameMap(Collection<ReferenceFrame> allReferenceFrames)
   {
      Map<String, ReferenceFrame> newFullnameToReferenceFrameMap = new LinkedHashMap<>();
      allReferenceFrames.forEach(frame -> newFullnameToReferenceFrameMap.put(getFullname(frame), frame));
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> fullnameToReferenceFrameMapProperty.set(newFullnameToReferenceFrameMap));
   }

   public static String getFullname(ReferenceFrame referenceFrame)
   {
      StringBuilder fullname = new StringBuilder();

      ReferenceFrame[] ancestors = referenceFrame.getFramesStartingWithRootEndingWithThis();

      for (int i = 0; i < ancestors.length - 1; i++)
         fullname.append(ancestors[i].getName()).append('.');
      fullname.append(referenceFrame.getName());

      return fullname.toString();
   }

   private static List<String> getFrameNamespace(ReferenceFrame referenceFrame)
   {
      List<String> namespace = new ArrayList<>();
      for (ReferenceFrame ancestor = referenceFrame.getParent(); ancestor != null; ancestor = ancestor.getParent())
         namespace.add(0, ancestor.getName());
      return namespace;
   }

   public ReferenceFrame getWorldFrame()
   {
      return worldFrame;
   }

   public Collection<ReferenceFrame> getReferenceFrames()
   {
      if (uniqueNameToReferenceFrameMapProperty.get() == null)
         return Collections.emptyList();
      return uniqueNameToReferenceFrameMapProperty.get().values();
   }

   public Collection<String> getReferenceFrameUniqueNames()
   {
      if (uniqueNameToReferenceFrameMapProperty.get() == null)
         return Collections.emptyList();
      return uniqueNameToReferenceFrameMapProperty.get().keySet();
   }

   public Collection<String> getReferenceFrameFullnames()
   {
      if (fullnameToReferenceFrameMapProperty.get() == null)
         return Collections.emptyList();
      return fullnameToReferenceFrameMapProperty.get().keySet();
   }

   public ReferenceFrame getReferenceFrameFromUniqueName(String uniqueName)
   {
      if (uniqueNameToReferenceFrameMapProperty.get() == null)
         return null;
      return uniqueNameToReferenceFrameMapProperty.get().get(uniqueName);
   }

   public ReferenceFrame getReferenceFrameFromFullname(String fullname)
   {
      if (fullnameToReferenceFrameMapProperty.get() == null)
         return null;
      return fullnameToReferenceFrameMapProperty.get().get(fullname);
   }

   public String getUniqueName(ReferenceFrame referenceFrame)
   {
      if (referenceFrameToUniqueNameMapProperty.get() == null)
         return null;
      return referenceFrameToUniqueNameMapProperty.get().get(referenceFrame);
   }

   public Map<ReferenceFrame, String> getReferenceFrameToUniqueNameMap()
   {
      return referenceFrameToUniqueNameMapProperty.get();
   }

   public Map<String, ReferenceFrame> getUniqueNameToReferenceFrameMap()
   {
      return uniqueNameToReferenceFrameMapProperty.get();
   }

   public Map<String, ReferenceFrame> getFullnameToReferenceFrameMap()
   {
      return fullnameToReferenceFrameMapProperty.get();
   }
}
