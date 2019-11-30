package us.ihmc.scs2.sessionVisualizer.managers;

import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.tools.YoVariableTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.CompositePropertyTools.YoVariableDatabase;
import us.ihmc.scs2.sharedMemory.LinkedBufferProperties;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.sharedMemory.LinkedYoVariableRegistry;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoManager extends AnimationTimer implements Manager
{
   private final LongProperty rootRegistryHashCodeProperty = new SimpleLongProperty(this, "rootRegistryHashCode", 0);

   private YoVariableRegistry rootRegistry;
   private LinkedYoVariableRegistry linkedRootRegistry;
   private LinkedYoVariableFactory linkedYoVariableFactory;

   private boolean updatingYoVariables = true;

   private YoVariableDatabase rootRegistryDatabase = null;

   public YoManager()
   {
   }

   @Override
   public void handle(long now)
   {
      if (linkedRootRegistry != null && !updatingYoVariables)
         linkedRootRegistry.pull();
   }

   @Override
   public void startSession(Session session)
   {
      LogTools.info("Linking YoVariables");
      rootRegistry = new YoVariableRegistry(SimulationSession.ROOT_REGISTRY_NAME);
      linkedYoVariableFactory = session.getLinkedYoVariableFactory();
      linkedRootRegistry = linkedYoVariableFactory.newLinkedYoVariableRegistry(rootRegistry);

      updatingYoVariables = true;
      linkedRootRegistry.linkNewYoVariables();
      linkedRootRegistry.pullMissingYoVariables();
      rootRegistryHashCodeProperty.set(YoVariableTools.hashCode(rootRegistry));
      rootRegistryDatabase = new YoVariableDatabase(rootRegistry);
      updatingYoVariables = false;
      LogTools.info("UI linked YoVariables created");
      start();
   }

   @Override
   public void stopSession()
   {
      rootRegistry = null;
      linkedYoVariableFactory = null;
      linkedRootRegistry = null;
      rootRegistryDatabase = null;
      rootRegistryHashCodeProperty.set(-1L);
   }

   @Override
   public boolean isSessionLoaded()
   {
      return linkedRootRegistry != null && !updatingYoVariables;
   }

   public LinkedYoVariableRegistry newLinkedYoVariableRegistry(YoVariableRegistry registry)
   {
      return linkedYoVariableFactory.newLinkedYoVariableRegistry(registry);
   }

   public LinkedYoVariable<?> newLinkedYoVariable(YoVariable<?> yoVariable)
   {
      return linkedYoVariableFactory.newLinkedYoVariable(yoVariable);
   }

   public void linkNewYoVariables()
   {
      updatingYoVariables = true;
      linkedRootRegistry.linkNewYoVariables();
      rootRegistryHashCodeProperty.set(YoVariableTools.hashCode(rootRegistry));
      updatingYoVariables = false;
   }

   public void pullMissingYoVariables()
   {
      updatingYoVariables = true;
      linkedRootRegistry.pullMissingYoVariables();
      rootRegistryHashCodeProperty.set(YoVariableTools.hashCode(rootRegistry));
      updatingYoVariables = false;
   }

   public YoVariableRegistry getRootRegistry()
   {
      return rootRegistry;
   }

   public YoVariableDatabase getRootRegistryDatabase()
   {
      return rootRegistryDatabase;
   }

   public boolean isUpdatingYoVariables()
   {
      return updatingYoVariables;
   }

   public LinkedYoVariableRegistry getLinkedRootRegistry()
   {
      return linkedRootRegistry;
   }

   public LinkedBufferProperties newLinkedBufferProperties()
   {
      if (linkedYoVariableFactory == null)
         return null;
      else
         return linkedYoVariableFactory.newLinkedBufferProperties();
   }

   public LongProperty rootRegistryHashCodeProperty()
   {
      return rootRegistryHashCodeProperty;
   }

   public int getBufferSize()
   {
      if (linkedRootRegistry == null)
         return -1;
      if (linkedRootRegistry.peekCurrentBufferProperties() == null)
         return -1;
      return linkedRootRegistry.peekCurrentBufferProperties().getSize();
   }
}
