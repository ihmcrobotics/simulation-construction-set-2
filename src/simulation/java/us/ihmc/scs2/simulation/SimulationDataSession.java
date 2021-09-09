package us.ihmc.scs2.simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.DefinitionIOTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionIOTools;

public class SimulationDataSession extends Session
{
   private final String sessionName;
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final List<TerrainObjectDefinition> terrainObjectDefinitions = new ArrayList<>();
   private final List<YoGraphicDefinition> yoGraphicDefinitions = new ArrayList<>();

   public SimulationDataSession(File dataDirectory) throws FileNotFoundException, JAXBException, IOException
   {
      sessionName = dataDirectory.getName();

      for (File childFile : dataDirectory.listFiles())
      {
         if (childFile.getName().endsWith(SessionIOTools.scsRobotDefinitionFileExtension))
         {
            robotDefinitions.add(DefinitionIOTools.loadRobotDefinition(new FileInputStream(childFile)));
         }
         else if (childFile.getName().endsWith(SessionIOTools.scsTerrainDefinitionFileExtension))
         {
            terrainObjectDefinitions.add(DefinitionIOTools.loadTerrainObjectDefinition(new FileInputStream(childFile)));
         }
         else if (childFile.getName().endsWith(SessionIOTools.yoGraphicConfigurationFileExtension))
         {
            yoGraphicDefinitions.addAll(DefinitionIOTools.loadYoGraphicListDefinition(new FileInputStream(childFile)).getYoGraphics());
         }
         else if (childFile.getName().endsWith(SessionIOTools.yoRegistryDefinitionFileExtension))
         {

         }
         else
         {

         }
      }
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
