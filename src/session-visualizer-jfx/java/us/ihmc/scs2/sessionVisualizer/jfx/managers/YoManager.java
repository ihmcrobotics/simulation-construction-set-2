package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ObservedAnimationTimer;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.YoVariableTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools.YoVariableDatabase;
import us.ihmc.scs2.sharedMemory.LinkedBufferProperties;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;
import us.ihmc.scs2.sharedMemory.LinkedYoVariable;
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
import us.ihmc.scs2.simulation.SimulationSession;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoManager extends ObservedAnimationTimer implements Manager
{
   private final LongProperty rootRegistryHashCodeProperty = new SimpleLongProperty(this, "rootRegistryHashCode", 0);

   private YoRegistry rootRegistry;
   private LinkedYoRegistry linkedRootRegistry;
   private LinkedBufferProperties linkedBufferProperties;
   private LinkedYoVariableFactory linkedYoVariableFactory;

   private boolean updatingYoVariables = true;

   private YoVariableDatabase rootRegistryDatabase = null;

   public YoManager()
   {
   }

   @Override
   public void handleImpl(long now)
   {
      if (linkedRootRegistry != null && !updatingYoVariables)
         linkedRootRegistry.pull();
   }

   @Override
   public void startSession(Session session)
   {
      LogTools.info("Linking YoVariables");
      rootRegistry = new YoRegistry(SimulationSession.ROOT_REGISTRY_NAME);
      linkedYoVariableFactory = session.getLinkedYoVariableFactory();
      linkedRootRegistry = linkedYoVariableFactory.newLinkedYoRegistry(rootRegistry);
      linkedBufferProperties = linkedYoVariableFactory.newLinkedBufferProperties();

      updatingYoVariables = true;
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
      linkedBufferProperties = null;
      rootRegistryDatabase = null;
      rootRegistryHashCodeProperty.set(-1L);
   }

   @Override
   public boolean isSessionLoaded()
   {
      return linkedRootRegistry != null && !updatingYoVariables;
   }

   public LinkedYoRegistry newLinkedYoRegistry(YoRegistry registry)
   {
      return linkedYoVariableFactory.newLinkedYoRegistry(registry);
   }

   public LinkedYoVariable<?> newLinkedYoVariable(YoVariable yoVariable)
   {
      return linkedYoVariableFactory.newLinkedYoVariable(yoVariable);
   }

   public YoRegistry getRootRegistry()
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

   public LinkedYoRegistry getLinkedRootRegistry()
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
      if (linkedBufferProperties == null)
         return -1;
      if (linkedBufferProperties.peekCurrentBufferProperties() == null)
         return -1;
      return linkedBufferProperties.peekCurrentBufferProperties().getSize();
   }
}
