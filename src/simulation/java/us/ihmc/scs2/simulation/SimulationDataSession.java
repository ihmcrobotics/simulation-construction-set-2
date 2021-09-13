package us.ihmc.scs2.simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import us.ihmc.scs2.definition.SessionInformationDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.DefinitionIOTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionIOTools;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools.DataFormat;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;

public class SimulationDataSession extends Session
{
   private final String sessionName;
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final List<TerrainObjectDefinition> terrainObjectDefinitions = new ArrayList<>();
   private final List<YoGraphicDefinition> yoGraphicDefinitions = new ArrayList<>();

   public SimulationDataSession(File sessionInfoFile) throws FileNotFoundException, JAXBException, IOException
   {
      SessionInformationDefinition sessionInfo = DefinitionIOTools.loadSessionInformationDefinition(new FileInputStream(sessionInfoFile));
      sessionName = sessionInfo.getSessionName();
      setSessionDTSeconds(sessionInfo.getSessionDTSeconds());

      File dataDirectory = sessionInfoFile.getParentFile();

      for (String robotFileName : sessionInfo.getRobotFileNames())
      {
         File robotFile = new File(dataDirectory, robotFileName);
         RobotDefinition robotDefinition = DefinitionIOTools.loadRobotDefinition(new FileInputStream(robotFile));
         Path resourceDirectory = Paths.get(dataDirectory.getAbsolutePath(), "resources", robotDefinition.getName());
         robotDefinition.setResourceClassLoader(new URLClassLoader(new URL[] {resourceDirectory.toUri().toURL()}));
         robotDefinitions.add(robotDefinition);
      }

      for (String terrainFileName : sessionInfo.getTerrainFileNames())
      {
         File robotFile = new File(dataDirectory, terrainFileName);
         TerrainObjectDefinition terrainObjectDefinition = DefinitionIOTools.loadTerrainObjectDefinition(new FileInputStream(robotFile));
         Path resourceDirectory = Paths.get(dataDirectory.getAbsolutePath(),
                                            "resources",
                                            terrainFileName.replace(SessionIOTools.terrainObjectDefinitionFileExtension, ""));
         terrainObjectDefinition.setResourceClassLoader(new URLClassLoader(new URL[] {resourceDirectory.toUri().toURL()}));
         terrainObjectDefinitions.add(terrainObjectDefinition);
      }

      if (sessionInfo.getGraphicFileName() != null)
      {
         File graphicFile = new File(dataDirectory, sessionInfo.getGraphicFileName());
         yoGraphicDefinitions.addAll(DefinitionIOTools.loadYoGraphicListDefinition(new FileInputStream(graphicFile)).getYoGraphics());
      }

      if (sessionInfo.getRegistryFileName() != null)
      {
         File registryFile = new File(dataDirectory, sessionInfo.getRegistryFileName());
         SharedMemoryTools.duplicateMissingYoVariablesInTarget(SharedMemoryIOTools.importRegistry(new FileInputStream(registryFile)), rootRegistry);
      }

      if (sessionInfo.getDataFileName() != null)
      {
         Objects.requireNonNull(sessionInfo.getRegistryFileName());
         File dataFile = new File(dataDirectory, sessionInfo.getDataFileName());

         sharedBuffer.resizeBuffer(1); // Let the data importer configure the buffer size.

         switch (DataFormat.fromFilename(dataFile.getName()))
         {
            case ASCII:
               SharedMemoryIOTools.importDataASCII(new FileInputStream(dataFile), sharedBuffer);
               break;
            case CSV:
               SharedMemoryIOTools.importDataCSV(new FileInputStream(dataFile), sharedBuffer);
               break;
            case MATLAB:
               SharedMemoryIOTools.importDataMatlab(dataFile, sharedBuffer);
               break;
            default:
               throw new IllegalStateException("Unhandled data format: " + dataFile.getName());
         }
      }

      hasBufferSizeBeenInitialized = true;
      hasBufferRecordPeriodBeenInitialized = true;

      setSessionModeTask(SessionMode.RUNNING, () ->
      {
         setSessionMode(SessionMode.PAUSE);
      });
      setSessionState(SessionState.ACTIVE);
   }

   @Override
   protected double doSpecificRunTick()
   {
      return 0;
   }

   @Override
   public String getSessionName()
   {
      return sessionName;
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotDefinitions;
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return terrainObjectDefinitions;
   }

   @Override
   public List<YoGraphicDefinition> getYoGraphicDefinitions()
   {
      return yoGraphicDefinitions;
   }
}
