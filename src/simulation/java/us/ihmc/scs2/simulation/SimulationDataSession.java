package us.ihmc.scs2.simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import us.ihmc.scs2.session.SessionIOTools.DataFormat;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools;
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
         robotDefinitions.add(DefinitionIOTools.loadRobotDefinition(new FileInputStream(robotFile)));
      }

      for (String terrainFileName : sessionInfo.getTerrainFileNames())
      {
         File robotFile = new File(dataDirectory, terrainFileName);
         terrainObjectDefinitions.add(DefinitionIOTools.loadTerrainObjectDefinition(new FileInputStream(robotFile)));
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
