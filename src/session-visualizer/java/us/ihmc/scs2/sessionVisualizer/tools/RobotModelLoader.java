package us.ihmc.scs2.sessionVisualizer.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;

import gnu.trove.map.hash.TLongObjectHashMap;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tools.EuclidHashCodeTools;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointBasics;
import us.ihmc.mecano.multiBodySystem.interfaces.SixDoFJointBasics;
import us.ihmc.mecano.multiBodySystem.iterators.SubtreeStreams;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoMultiBodySystem;
import us.ihmc.robotDataLogger.handshake.YoVariableHandshakeParser;
import us.ihmc.robotDataLogger.jointState.JointState;
import us.ihmc.robotDataLogger.jointState.OneDoFState;
import us.ihmc.robotDataLogger.jointState.SixDoFState;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.sdf.SDFTools;
import us.ihmc.scs2.definition.robot.sdf.items.SDFRoot;
import us.ihmc.tools.ClassLoaderTools;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class RobotModelLoader
{
   private final static String resourceDirectoryLocation = System.getProperty("user.home") + File.separator + ".ihmc" + File.separator + "resources";

   private static final TLongObjectHashMap<RobotDefinition> cachedImportedModels = new TLongObjectHashMap<>();

   public static Runnable setupRobotUpdater(RobotDefinition robotDefinition, YoVariableHandshakeParser handshakeParser, YoVariableRegistry rootRegistry)
   {
      if (robotDefinition == null)
         return null;

      YoVariableRegistry robotRegistry = new YoVariableRegistry(robotDefinition.getName());
      MultiBodySystemBasics multiBodySystemBasics = robotDefinition.toMultiBodySystemBasics(ReferenceFrame.getWorldFrame());
      YoMultiBodySystem robot = new YoMultiBodySystem(multiBodySystemBasics, ReferenceFrame.getWorldFrame(), robotRegistry);

      Map<String, JointState> jointNameToState = handshakeParser.getJointStates().stream().collect(Collectors.toMap(JointState::getName, Function.identity()));

      List<Runnable> jointStateUpdaters = new ArrayList<>();

      SubtreeStreams.fromChildren(OneDoFJointBasics.class, robot.getRootBody()).forEach(oneDofJoint ->
      {
         OneDoFState jointState = (OneDoFState) jointNameToState.get(oneDofJoint.getName());
         jointStateUpdaters.add(() -> oneDofJoint.setQ(jointState.getQ()));
      });

      SixDoFJointBasics floatingJoint = (SixDoFJointBasics) robot.getRootBody().getChildrenJoints().get(0);
      jointStateUpdaters.add(() ->
      {
         SixDoFState jointState = (SixDoFState) jointNameToState.get(floatingJoint.getSuccessor().getName());
         floatingJoint.getJointPose().set(jointState.getTranslation(), jointState.getRotation());
      });

      rootRegistry.addChild(robotRegistry);

      return () -> jointStateUpdaters.forEach(updater -> updater.run());
   }

   public static RobotDefinition loadModel(String modelName, String[] resourceDirectories, byte[] model, byte[] resourceZip)
   {
      if (model == null)
         return null;

      long modelHashCode = computeModelHashCode(modelName, resourceDirectories, model, resourceZip);
      RobotDefinition robotDefinition = cachedImportedModels.get(modelHashCode);

      if (robotDefinition != null)
         return robotDefinition;

      upackResources(modelName, resourceZip);
      try
      {
         SDFRoot sdfRoot = SDFTools.loadSDFRoot(new ByteArrayInputStream(model), Arrays.asList(resourceDirectories));
         robotDefinition = SDFTools.toFloatingRobotDefinition(sdfRoot.getModels().get(0));
         cachedImportedModels.put(modelHashCode, robotDefinition);
         return robotDefinition;
      }
      catch (JAXBException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   private static void upackResources(String modelName, byte[] resourceZip)
   {
      // Some of the model importers do not immediately release the files, triggering GC seems to help ensuring that these files are released.
      System.gc();

      if (resourceZip != null)
      {
         Path resourceDirectory = Paths.get(resourceDirectoryLocation, modelName);
         try
         {
            Files.createDirectories(resourceDirectory);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }

         ByteArrayInputStream is = new ByteArrayInputStream(resourceZip);
         ZipInputStream zip = new ZipInputStream(is);
         ZipEntry ze = null;

         try
         {
            while ((ze = zip.getNextEntry()) != null)
            {
               Path target = resourceDirectory.resolve(ze.getName());
               Files.createDirectories(target.getParent());
               Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
            }
         }
         catch (IOException e)
         {
            System.err.println("SDFModelLoader: Cannot load model zip file. Not unpacking robot model.");
            e.printStackTrace();
         }
         finally
         {
            try
            {
               zip.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }

         try
         {
            ClassLoaderTools.addURLToSystemClassLoader(resourceDirectory.toUri().toURL());
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   private static long computeModelHashCode(String modelName, String[] resourceDirectories, byte[] model, byte[] resourceZip)
   {
      long hash = modelName == null ? 1L : modelName.hashCode();
      hash = EuclidHashCodeTools.combineHashCode(hash, Arrays.hashCode(resourceDirectories));
      hash = EuclidHashCodeTools.combineHashCode(hash, Arrays.hashCode(model));
      hash = EuclidHashCodeTools.combineHashCode(hash, Arrays.hashCode(resourceZip));
      return hash;
   }
}
