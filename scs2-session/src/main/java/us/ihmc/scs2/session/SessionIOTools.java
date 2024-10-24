package us.ihmc.scs2.session;

import org.apache.commons.io.FileUtils;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.DefinitionIOTools;
import us.ihmc.scs2.definition.SessionInformationDefinition;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.RobotStateDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicListDefinition;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools.DataFormat;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoVariable;

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SessionIOTools
{
   public static final Path SCS2_HOME;
   public static final Path SCS2_TEMP_FOLDER_PATH;

   static
   {
      String scs2home = System.getProperty("scs2.home");

      if (scs2home == null)
      {
         scs2home = System.getenv("SCS2_HOME");
      }

      SCS2_HOME = scs2home != null ? Paths.get(scs2home) : Paths.get(System.getProperty("user.home"), ".ihmc", "scs2");
      SCS2_TEMP_FOLDER_PATH = SCS2_HOME.resolve(".temp");

      try
      {
         FileTools.ensureDirectoryExists(SCS2_HOME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public static final String infoFileExtension = ".scs2.info";
   public static final String robotDefinitionFileExtension = ".scs2.robot";
   public static final String robotStateDefinitionFileExtension = ".scs2.robotState";
   public static final String terrainObjectDefinitionFileExtension = ".scs2.terrain";
   public static final String yoGraphicConfigurationFileExtension = ".scs2.yoGraphic";
   public static final String yoRegistryDefinitionFileExtension = ".scs2.registry";

   public static void exportSessionData(Session session, SessionDataExportRequest request) throws JAXBException, IOException, URISyntaxException
   {
      if (request.getOnExportStartCallback() != null)
      {
         try
         {
            request.getOnExportStartCallback().run();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      try
      {
         exportSessionDataImpl(session, request);
      }
      finally
      {
         if (request.getOnExportEndCallback() != null)
         {
            try
            {
               request.getOnExportEndCallback().run();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   private static void exportSessionDataImpl(Session session, SessionDataExportRequest request) throws JAXBException, IOException, URISyntaxException
   {
      File file = request.getFile();

      if (file.exists())
      {
         if (file.isFile())
         {
            if (!request.getOverwrite())
            {
               LogTools.error("File exists and overwrite is set to false: {}", file);
               return;
            }

            if (!file.delete())
            {
               LogTools.error("Could not delete file: {}", file);
               return;
            }

            if (!file.mkdir())
            {
               LogTools.error("Could not create directory: {}", file);
               return;
            }
         }
         else if (file.isDirectory())
         {
            if (file.list().length > 0)
            {
               // Clean up folder
               String[] allExtensions = {infoFileExtension,
                                         robotDefinitionFileExtension,
                                         terrainObjectDefinitionFileExtension,
                                         yoGraphicConfigurationFileExtension,
                                         yoRegistryDefinitionFileExtension};
               allExtensions = SharedMemoryTools.concatenate(allExtensions,
                                                             Arrays.stream(DataFormat.values()).map(DataFormat::getFileExtension).toArray(String[]::new));

               for (File childFile : file.listFiles())
               {
                  if (childFile.isDirectory() && childFile.getName().equals("resources"))
                  {
                     if (!request.getOverwrite())
                     {
                        LogTools.error("Cannot delete file ({}) because overwrite is set to false.", childFile);
                        return;
                     }
                     else if (!emptyDirectory(childFile))
                     {
                        LogTools.error("Could not empty directory: {}", childFile);
                        return;
                     }
                     else
                     {
                        continue;
                     }
                  }

                  for (String extension : allExtensions)
                  {
                     if (childFile.getName().endsWith(extension))
                     {
                        if (!request.getOverwrite())
                        {
                           LogTools.error("Cannot delete file ({}) because overwrite is set to false.", childFile);
                           return;
                        }
                        else if (!childFile.delete())
                        {
                           LogTools.error("Could not delete file: {}", childFile);
                           return;
                        }
                        else
                        {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
      else
      {
         // File does not exist
         if (!file.mkdirs())
         {
            LogTools.error("Unable to create parent directies of {}", file);
            return;
         }
      }

      SessionInformationDefinition sessionInfo = new SessionInformationDefinition();
      sessionInfo.setSessionName(session.getSessionName());
      sessionInfo.setSessionDTSeconds(session.getSessionDTSeconds());
      sessionInfo.setRecordDTSeconds(session.getSessionDTSeconds() * session.getBufferRecordTickPeriod());

      File resourcesDirectory = new File(file, "resources");
      resourcesDirectory.mkdir();

      if (request.getExportRobotDefinitions())
      {
         for (RobotDefinition robotDefinition : session.getRobotDefinitions())
         {
            String name = robotDefinition.getName();
            File robotFile = new File(file, name + robotDefinitionFileExtension);
            ClassLoader resourceClassLoader = robotDefinition.getResourceClassLoader();
            if (resourceClassLoader == null)
               resourceClassLoader = SessionIOTools.class.getClassLoader();
            File robotResourceDirectory = new File(resourcesDirectory, name);
            LogTools.info("Exporting RobotDefinition for: {} File: {}", name, robotFile);
            DefinitionIOTools.saveRobotDefinitionAndResources(robotFile, robotDefinition, robotResourceDirectory, resourceClassLoader);
            sessionInfo.getRobotFileNames().add(robotFile.getName());
         }
      }

      if (request.getExportTerrainObjectDefinitions())
      {
         Set<String> terrainNames = new HashSet<>();

         for (TerrainObjectDefinition terrainObjectDefinition : session.getTerrainObjectDefinitions())
         {
            String name = terrainObjectDefinition.getName();
            if (name == null || name.isEmpty())
               name = "terrain";

            if (terrainNames.contains(name))
            {
               int index = 1;
               String uniqueName = name + "_" + index;
               while (terrainNames.contains(uniqueName))
               {
                  index++;
                  uniqueName = name + "_" + index;
               }
               name = uniqueName;
            }

            terrainNames.add(name);

            File terrainFile = new File(file, name + terrainObjectDefinitionFileExtension);
            File terrainResourceDirectory = new File(resourcesDirectory, name);
            LogTools.info("Exporting TerrainObjectDefinition for: {}. File: {}", name, terrainFile);

            ClassLoader resourceClassLoader = terrainObjectDefinition.getResourceClassLoader();
            if (resourceClassLoader == null)
               resourceClassLoader = SessionIOTools.class.getClassLoader();
            DefinitionIOTools.saveTerrainObjectDefinitionAndResources(terrainFile, terrainObjectDefinition, terrainResourceDirectory, resourceClassLoader);
            sessionInfo.getTerrainFileNames().add(terrainFile.getName());
         }
      }

      if (request.getExportSessionYoGraphicDefinitions())
      {
         File graphicFile = new File(file, "sessionGraphics" + yoGraphicConfigurationFileExtension);
         LogTools.info("Exporting session yoGraphics. File: {}", graphicFile);
         File yoGraphicResourceDirectory = new File(resourcesDirectory, "yoGraphics");
         DefinitionIOTools.saveYoGraphicListDefinitionAndResources(graphicFile,
                                                                   new YoGraphicListDefinition(session.getYoGraphicDefinitions()),
                                                                   yoGraphicResourceDirectory);
         sessionInfo.setGraphicFileName(graphicFile.getName());
      }

      Predicate<YoVariable> variableFilter = request.getVariableFilter();
      Predicate<YoRegistry> registryFilter = request.getRegistryFilter();
      if (request.getExportSessionBufferRegistryDefinition())
      {
         File registryFile = new File(file, "variables" + yoRegistryDefinitionFileExtension);
         LogTools.info("Exporting session variable structure. File: {}", registryFile);
         SharedMemoryIOTools.exportRegistry(session.getRootRegistry(), new FileOutputStream(registryFile), variableFilter, registryFilter);
         sessionInfo.setRegistryFileName(registryFile.getName());
      }

      if (request.getExportRobotStateDefinitions())
      {
         List<RobotStateDefinition> currentRobotStateDefinitions = session.getCurrentRobotStateDefinitions(true);
         if (currentRobotStateDefinitions != null)
         {
            for (RobotStateDefinition robotStateDefinition : currentRobotStateDefinitions)
            {
               String name = robotStateDefinition.getRobotName() + "State";
               File robotStateFile = new File(file, name + robotStateDefinitionFileExtension);
               LogTools.info("Exporting RobotStateDefinition for: {} File: {}", name, robotStateFile);
               DefinitionIOTools.saveRobotStateDefinition(new FileOutputStream(robotStateFile), robotStateDefinition);
               sessionInfo.getRobotStateFileNames().add(robotStateFile.getName());
            }
         }
      }

      if (request.getExportSessionBufferDataFormat() != null)
      {
         File dataFile = new File(file, "data" + request.getExportSessionBufferDataFormat().getFileExtension());
         LogTools.info("Exporting session data. File: {}", dataFile);
         YoSharedBuffer buffer = session.getBuffer();
         switch (request.getExportSessionBufferDataFormat())
         {
            case ASCII:
               buffer.exportDataASCII(new FileOutputStream(dataFile), variableFilter, registryFilter);
               break;
            case CSV:
               buffer.exportDataCSV(new FileOutputStream(dataFile), variableFilter, registryFilter);
               break;
            case MATLAB:
               buffer.exportDataMatlab(dataFile, variableFilter, registryFilter);
               break;
            default:
               LogTools.error("Unhandled data format: {}", request.getExportSessionBufferDataFormat());
               break;
         }
         sessionInfo.setDataFileName(dataFile.getName());
         LogTools.info("Done exporting session data.");
      }

      File sessionInfoFile = new File(file, "session" + infoFileExtension);
      DefinitionIOTools.saveSessionInformationDefinition(new FileOutputStream(sessionInfoFile), sessionInfo);
   }

   public static boolean emptyDirectory(File directoryToEmpty)
   {
      if (!directoryToEmpty.isDirectory())
         return false;

      try
      {
         FileUtils.deleteDirectory(directoryToEmpty);
         directoryToEmpty.mkdir();
         return true;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return false;
      }
   }

   public static File getTemporaryDirectory(String directoryName)
   {
      File tempDir = SCS2_TEMP_FOLDER_PATH.resolve(directoryName).toFile();

      if (tempDir.exists())
      {
         try
         {
            FileUtils.forceDelete(tempDir);
         }
         catch (IOException e)
         {
            e.printStackTrace();
            return null;
         }
      }
      tempDir.mkdirs();

      return tempDir;
   }

   public static void unzipFile(File input, File destination) throws IOException
   {
      ZipInputStream zis = null;
      int length;
      byte[] buffer = new byte[1024];

      try
      {
         zis = new ZipInputStream(new FileInputStream(input));
         ZipEntry ze = zis.getNextEntry();

         while (ze != null)
         {
            File newFile = newFile(destination, ze);

            if (ze.isDirectory())
            {
               if (!newFile.isDirectory() && !newFile.mkdirs())
                  throw new IOException("Failed to create directory " + newFile);
            }
            else
            {
               File parent = newFile.getParentFile();

               if (!parent.isDirectory() && !parent.mkdirs())
                  throw new IOException("Failed to create directory " + parent);

               FileOutputStream fos = new FileOutputStream(newFile);
               while ((length = zis.read(buffer)) > 0)
                  fos.write(buffer, 0, length);
               fos.close();
            }

            ze = zis.getNextEntry();
         }
      }
      finally
      {
         if (zis != null)
         {
            zis.closeEntry();
            zis.close();
         }
      }
   }

   public static void zipFile(File input, File destination) throws IOException
   {
      try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destination)))
      {
         if (input.isDirectory())
         {
            for (File subFile : input.listFiles())
               zipFile(subFile.getName(), subFile, zipOut);
         }
         else
         {
            zipFile(input.getName(), input, zipOut);
         }
      }
   }

   public static void zipFile(String fileName, File fileToZip, ZipOutputStream zipOut) throws IOException
   {
      if (fileToZip.isHidden())
      {
         return;
      }

      if (fileToZip.isDirectory())
      {
         if (fileName.endsWith("/"))
            zipOut.putNextEntry(new ZipEntry(fileName));
         else
            zipOut.putNextEntry(new ZipEntry(fileName + "/"));

         zipOut.closeEntry();

         File[] children = fileToZip.listFiles();

         for (File childFile : children)
         {
            zipFile(fileName + "/" + childFile.getName(), childFile, zipOut);
         }
      }
      else
      {
         try (FileInputStream fis = new FileInputStream(fileToZip))
         {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;

            while ((length = fis.read(bytes)) >= 0)
               zipOut.write(bytes, 0, length);
         }
      }
   }

   public static File newFile(File destinationParent, ZipEntry zipEntry) throws IOException
   {
      File destinationFile = new File(destinationParent, zipEntry.getName());

      if (destinationFile.getCanonicalPath().startsWith(destinationParent.getCanonicalPath() + File.separator))
         return destinationFile;
      else
         throw new IOException("Attempted to unzip outside destination: " + zipEntry.getName());
   }
}
