package us.ihmc.scs2.sessionVisualizer.jfx;

import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.SCS2_CONFIGURATION_DEFAULT_PATH;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.scsConfigurationFileExtension;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.yoChartGroupConfigurationFileExtension;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.yoCompositeConfigurationFileExtension;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.yoEntryConfigurationFileExtension;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.yoGraphicConfigurationFileExtension;
import static us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools.yoSliderboardConfigurationFileExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.scs2.definition.configuration.SCSGuiConfigurationDefinition;
import us.ihmc.scs2.definition.configuration.WindowConfigurationDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;

public class SCSGuiConfiguration
{
   private final String robotName;
   private final String simulationName;

   private final String configurationName;

   private final String mainConfigurationFilename;
   private String yoGraphicsFilename;
   private String yoCompositeFilename;
   private String yoEntryConfigurationFilename;
   private String yoSliderboardConfigurationFilename;
   private String mainYoChartGroupFilename;
   private final List<String> secondaryYoChartGroupFilenames = new ArrayList<>();

   private final Path mainConfigurationPath;
   private Path yoGraphicsPath;
   private Path yoCompositePath;
   private Path yoEntryConfigurationPath;
   private Path yoSliderboardConfigurationPath;
   private Path mainYoChartGroupPath;
   private final List<Path> secondaryYoChartGroupPaths = new ArrayList<>();

   private SCSGuiConfigurationDefinition definition;

   public static SCSGuiConfiguration defaultLoader(String robotName, String simulationName)
   {
      try
      {
         return new SCSGuiConfiguration(robotName, simulationName, -1, true);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public static SCSGuiConfiguration defaultSaver(String robotName, String simulationName, int numberOfSecondaryChartWindows)
   {
      try
      {
         return new SCSGuiConfiguration(robotName, simulationName, numberOfSecondaryChartWindows, false);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   private SCSGuiConfiguration(String robotName, String simulationName, int numberOfSecondaryChartWindows, boolean isLoading) throws IOException, JAXBException
   {
      this.robotName = robotName;
      this.simulationName = simulationName;
      configurationName = robotName + "-" + simulationName;

      mainConfigurationFilename = toFilename("Main", scsConfigurationFileExtension);
      mainConfigurationPath = toPath(mainConfigurationFilename);

      if (isLoading)
      {
         if (mainConfigurationPath.toFile().exists())
         {
            try
            {
               definition = XMLTools.loadSCSGuiConfigurationDefinition(new FileInputStream(mainConfigurationPath.toFile()));

               yoGraphicsFilename = definition.getYoGraphicsFilename();
               yoCompositeFilename = definition.getYoCompositePatternListFilename();
               yoEntryConfigurationFilename = definition.getYoEntryConfigurationFilename();
               yoSliderboardConfigurationFilename = definition.getYoSliderboardConfigurationFilename();
               mainYoChartGroupFilename = definition.getMainYoChartGroupConfigurationFilename();
               if (definition.getSecondaryYoChartGroupConfigurationsFilenames() != null)
                  secondaryYoChartGroupFilenames.addAll(definition.getSecondaryYoChartGroupConfigurationsFilenames());

               yoGraphicsPath = toPath(yoGraphicsFilename);
               yoCompositePath = toPath(yoCompositeFilename);
               yoEntryConfigurationPath = toPath(yoEntryConfigurationFilename);
               yoSliderboardConfigurationPath = toPath(yoSliderboardConfigurationFilename);
               mainYoChartGroupPath = toPath(mainYoChartGroupFilename);

               for (String filename : secondaryYoChartGroupFilenames)
               {
                  secondaryYoChartGroupPaths.add(toPath(filename));
               }

               for (int i = secondaryYoChartGroupFilenames.size() - 1; i >= 0; i--)
               {
                  if (!secondaryYoChartGroupPaths.get(i).toFile().exists())
                  {
                     secondaryYoChartGroupFilenames.remove(i);
                     secondaryYoChartGroupPaths.remove(i);
                  }
               }
            }
            catch (FileNotFoundException | JAXBException e)
            {
               e.printStackTrace();
            }
         }
      }
      else
      {
         FileTools.ensureFileExists(mainConfigurationPath);

         yoGraphicsFilename = toFilename("YoGraphics", yoGraphicConfigurationFileExtension);
         yoCompositeFilename = toFilename("YoComposite", yoCompositeConfigurationFileExtension);
         yoEntryConfigurationFilename = toFilename("YoEntry", yoEntryConfigurationFileExtension);
         yoSliderboardConfigurationFilename = toFilename("YoSliderboard", yoSliderboardConfigurationFileExtension);
         mainYoChartGroupFilename = toFilename("MainYoChartGroup", yoChartGroupConfigurationFileExtension);

         yoGraphicsPath = toPath(yoGraphicsFilename);
         yoCompositePath = toPath(yoCompositeFilename);
         yoEntryConfigurationPath = toPath(yoEntryConfigurationFilename);
         yoSliderboardConfigurationPath = toPath(yoSliderboardConfigurationFilename);
         mainYoChartGroupPath = toPath(mainYoChartGroupFilename);

         for (int i = 0; i < numberOfSecondaryChartWindows; i++)
         {
            String filename = toFilename("SecondaryYoChartGroup" + i, yoChartGroupConfigurationFileExtension);
            secondaryYoChartGroupFilenames.add(filename);
            secondaryYoChartGroupPaths.add(toPath(filename));
         }

         definition = new SCSGuiConfigurationDefinition();
         definition.setName(configurationName);
         definition.setYoGraphicsFilename(yoGraphicsFilename);
         definition.setYoCompositePatternListFilename(yoCompositeFilename);
         definition.setYoEntryConfigurationFilename(yoEntryConfigurationFilename);
         definition.setYoSliderboardConfigurationFilename(yoSliderboardConfigurationFilename);
         definition.setMainYoChartGroupConfigurationFilename(mainYoChartGroupFilename);
         definition.setSecondaryYoChartGroupConfigurationsFilenames(secondaryYoChartGroupFilenames);
      }
   }

   public void setBufferSize(int bufferSize)
   {
      definition.setBufferSize(bufferSize);
   }

   public void setShowOverheadPlotter(boolean showOverheadPlotter)
   {
      definition.setShowOverheadPlotter(showOverheadPlotter);
   }

   public void setShowAdvancedControls(boolean showAdvancedControls)
   {
      definition.setShowAdvancedControls(showAdvancedControls);
   }

   public void setMainStage(Stage stage)
   {
      definition.setMainWindowConfiguration(toWindowConfigurationDefinition(stage));
   }

   public void setSecondaryWindows(List<Stage> stages)
   {
      List<WindowConfigurationDefinition> definitions = stages.stream().map(SCSGuiConfiguration::toWindowConfigurationDefinition).collect(Collectors.toList());
      definition.setSecondaryWindowConfigurations(definitions);
   }

   public boolean exists()
   {
      return mainConfigurationPath.toFile().exists();
   }

   public void writeConfiguration()
   {
      try
      {
         XMLTools.saveSCSGuiConfigurationDefinition(new FileOutputStream(getMainConfigurationFile()), definition);
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public boolean hasYoGraphicsConfiguration()
   {
      return yoGraphicsPath != null && yoGraphicsPath.toFile().exists();
   }

   public boolean hasYoCompositeConfiguration()
   {
      return yoCompositePath != null && yoCompositePath.toFile().exists();
   }

   public boolean hasYoEntryConfiguration()
   {
      return yoEntryConfigurationPath != null && yoEntryConfigurationPath.toFile().exists();
   }

   public boolean hasYoSliderboardConfiguration()
   {
      return yoSliderboardConfigurationPath != null && yoSliderboardConfigurationPath.toFile().exists();
   }

   public boolean hasMainYoChartGroupConfiguration()
   {
      return mainYoChartGroupPath != null && mainYoChartGroupPath.toFile().exists();
   }

   public int getNumberOfSecondaryYoChartGroupConfigurations()
   {
      return secondaryYoChartGroupPaths.size();
   }

   public boolean hasBufferSize()
   {
      return definition != null && definition.getBufferSize() > 0;
   }

   public boolean hasMainWindowConfiguration()
   {
      return definition != null && definition.getMainWindowConfiguration() != null;
   }

   public boolean hasSecondaryWindowConfigurations()
   {
      return definition != null && definition.getSecondaryWindowConfigurations() != null
            && definition.getSecondaryWindowConfigurations().size() == getNumberOfSecondaryYoChartGroupConfigurations();
   }

   public File getMainConfigurationFile()
   {
      return mainConfigurationPath.toFile();
   }

   public File getYoGraphicsConfigurationFile()
   {
      return yoGraphicsPath.toFile();
   }

   public File getYoCompositeConfigurationFile()
   {
      return yoCompositePath.toFile();
   }

   public File getYoEntryConfigurationFile()
   {
      return yoEntryConfigurationPath.toFile();
   }

   public File getYoSliderboardConfigurationFile()
   {
      return yoSliderboardConfigurationPath.toFile();
   }

   public File getMainYoChartGroupConfigurationFile()
   {
      return mainYoChartGroupPath.toFile();
   }

   public File getSecondaryYoChartGroupConfigurationFile(int index)
   {
      return secondaryYoChartGroupPaths.get(index).toFile();
   }

   public int getBufferSize()
   {
      return definition.getBufferSize();
   }

   public boolean getShowOverheadPlotter()
   {
      return definition.isShowOverheadPlotter();
   }

   public boolean getShowAdvancedControls()
   {
      return definition.isShowAdvancedControls();
   }

   public void getMainWindowConfiguration(Stage stage)
   {
      loadWindowConfigurationDefinition(definition.getMainWindowConfiguration(), stage);
   }

   public void getSecondaryWindowConfigurations(List<Stage> stages)
   {
      for (int i = 0; i < definition.getSecondaryWindowConfigurations().size(); i++)
      {
         loadWindowConfigurationDefinition(definition.getSecondaryWindowConfigurations().get(i), stages.get(i));
      }
   }

   private String toFilename(String suffix, String extension)
   {
      return suffix + extension;
   }

   private Path toPath(String filename)
   {
      if (filename == null)
         return null;
      else
         return Paths.get(SCS2_CONFIGURATION_DEFAULT_PATH.toString(), robotName, simulationName, filename);
   }

   public String getRobotName()
   {
      return robotName;
   }

   public String getSimulationName()
   {
      return simulationName;
   }

   public static WindowConfigurationDefinition toWindowConfigurationDefinition(Stage stage)
   {
      WindowConfigurationDefinition definition = new WindowConfigurationDefinition();
      if (stage.isMaximized())
      {
         definition.setMaximized(true);
         definition.setPositionX(stage.getX());
         definition.setPositionY(stage.getY());
         definition.setWidth(stage.getWidth());
         definition.setHeight(stage.getHeight());
      }
      else
      {
         definition.setMaximized(false);
         definition.setPositionX(stage.getX());
         definition.setPositionY(stage.getY());
         definition.setWidth(stage.getWidth());
         definition.setHeight(stage.getHeight());
      }
      return definition;
   }

   public static void loadWindowConfigurationDefinition(WindowConfigurationDefinition definition, Stage stage)
   {
      double positionX = definition.getPositionX();
      double positionY = definition.getPositionY();
      double width;
      if (definition.getWidth() > 0.0)
         width = definition.getWidth();
      else
         width = stage.getWidth();

      double height;
      if (definition.getHeight() > 0.0)
         height = definition.getHeight();
      else
         height = stage.getHeight();

      ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(positionX, positionY, width, height);

      if (screensForRectangle.isEmpty())
      {
         // The window would be outside the visible bounds of the screens.
         // We'll reset the window so it appears in the middle of the primary screen.
         Screen primary = Screen.getPrimary();
         Rectangle2D visualBounds = primary.getVisualBounds();
         width = Math.min(width, visualBounds.getWidth());
         height = Math.min(height, visualBounds.getHeight());
         positionX = 0.5 * (visualBounds.getMinX() + visualBounds.getMaxX() - width);
         positionY = 0.5 * (visualBounds.getMinY() + visualBounds.getMaxY() - height);
      }

      stage.setX(positionX);
      stage.setY(positionY);

      if (definition.isMaximized())
      {
         stage.setMaximized(true);
      }
      else
      {
         stage.setWidth(width);
         stage.setHeight(height);
      }
   }
}
