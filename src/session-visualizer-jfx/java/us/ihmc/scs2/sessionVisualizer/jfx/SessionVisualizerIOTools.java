package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.prefs.Preferences;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import us.ihmc.commons.nio.FileTools;

public class SessionVisualizerIOTools
{
   private static final ClassLoader classLoader = SessionVisualizerIOTools.class.getClassLoader();

   public static final Path SCS2_HOME = Paths.get(System.getProperty("user.home"), "SCS2");
   public static final Path SCS2_CONFIGURATION_DEFAULT_PATH = Paths.get(SCS2_HOME.toString(), "Configurations");
   static
   {
      try
      {
         FileTools.ensureDirectoryExists(SCS2_HOME);
         FileTools.ensureDirectoryExists(SCS2_CONFIGURATION_DEFAULT_PATH);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public static final String scsConfigurationFileExtension = ".scs2";
   public static final String yoChartGroupConfigurationFileExtension = ".scs2.chart";
   public static final String yoGraphicConfigurationFileExtension = ".scs2.yoGraphic";
   public static final String yoCompositeConfigurationFileExtension = ".scs2.yoComposite";
   public static final String yoEntryConfigurationFileExtension = ".scs2.yoEntry";
   public static final String yoSliderboardConfigurationFileExtension = ".scs2.yoSliderboard";
   private static final ExtensionFilter yoChartGroupConfigurationFilter = new ExtensionFilter("SCS2 YoChartGroup File",
                                                                                              "*" + yoChartGroupConfigurationFileExtension);
   private static final ExtensionFilter yoGraphicConfigurationFilter = new ExtensionFilter("SCS2 YoGraphic File", "*" + yoGraphicConfigurationFileExtension);
   private static final ExtensionFilter yoCompositeConfigurationFilter = new ExtensionFilter("SCS2 YoComposite File",
                                                                                             "*" + yoCompositeConfigurationFileExtension);
   private static final ExtensionFilter yoEntryConfigurationFilter = new ExtensionFilter("SCS2 YoEntry File", "*" + yoEntryConfigurationFileExtension);
   private static final ExtensionFilter yoSliderboardConfigurationFilter = new ExtensionFilter("SCS2 YoSliderboard File",
                                                                                               "*" + yoSliderboardConfigurationFileExtension);

   private static final String CSS_FOLDER = "css/";
   private static final String FXML_FOLDER = "fxml/";
   private static final String ICONS_FOLDER = "icons/";
   private static final String IMAGES_FOLDER = "images/";
   private static final String SKYBOX_FOLDER = "skybox/";
   private static final String CONFIGURATION_FOLDER = "configuration/";
   private static final String YO_GRAPHIC_FOLDER = "yoGraphic/";
   private static final String SESSION_FOLDER = "session/";

   // CSS
   public static final URL GENERAL_STYLESHEET = getCSSResource("GeneralStylesheet");

   // Icon list:
   public static final Image SCS_ICON_IMAGE = loadIcon("scs-icon.png");
   public static final Image INVALID_ICON_IMAGE = loadIcon("invalid-icon.png");
   public static final Image VALID_ICON_IMAGE = loadIcon("valid-icon.png");
   public static final Image REMOTE_SESSION_IMAGE = loadIcon("viz-icon.png");
   public static final Image LOG_SESSION_IMAGE = loadIcon("log-icon.png");

   // FXML list:
   private static final String CHART = "chart/";
   private static final String YO_COMPOSITE = "yoComposite/";
   private static final String YO_COMPOSITE_PATTERN = YO_COMPOSITE + "pattern/";
   private static final String YO_COMPOSITE_SEARCH = YO_COMPOSITE + "search/";
   private static final String YO_COMPOSITE_ENTRY = YO_COMPOSITE + "entry/";
   private static final String YO_GRAPHIC = "yoGraphic/";
   private static final String YO_SLIDERBOARD = "yoSliderboard/";
   private static final String YO_SLIDERBOARD_BCF2000 = YO_SLIDERBOARD + "bcf2000/";

   // YoComposite resources:
   public static final String DEFAULT_YO_COMPOSITE_PATTERNS_FILE = "DefaultYoCompositePatterns" + yoCompositeConfigurationFileExtension;
   public static final URL YO_COMPOSITE_PATTERN_EDITOR_PANE_URL = getFXMLResource(YO_COMPOSITE_PATTERN, "YoCompositePatternEditorPane");
   public static final URL YO_COMPOSITE_PATTERN_PROPERTY_WINDOW_URL = getFXMLResource(YO_COMPOSITE_PATTERN, "YoCompositePatternPropertyWindow");
   public static final URL YO_COMPOSITE_SEARCH_PANEL_URL = getFXMLResource(YO_COMPOSITE_SEARCH, "YoCompositeSearchPane");
   public static final URL YO_SEARCH_OPTIONS_PANE_URL = getFXMLResource(YO_COMPOSITE_SEARCH, "YoSearchOptionsPane");
   public static final URL YO_SEARCH_TAB_PANE_URL = getFXMLResource(YO_COMPOSITE_SEARCH, "YoSearchTabPane");
   public static final URL YO_ENTRY_LIST_VIEW_URL = getFXMLResource(YO_COMPOSITE_ENTRY, "YoEntryListView");
   // YoGraphic resources:
   public static final URL GRAPHIC_2D_CIRCLE_URL = getYoGraphicResource("circle.svg");
   public static final URL GRAPHIC_2D_CIRCLE_CROSS_URL = getYoGraphicResource("circle_cross.svg");
   public static final URL GRAPHIC_2D_CIRCLE_PLUS_URL = getYoGraphicResource("circle_plus.svg");
   public static final URL GRAPHIC_2D_DIAMOND_URL = getYoGraphicResource("diamond.svg");
   public static final URL GRAPHIC_2D_SQUARE_URL = getYoGraphicResource("square.svg");
   public static final Path GRAPHIC_2D_CUSTOM_GRAPHICS = Paths.get(SCS2_HOME.toString(), "yoGraphic");
   public static final URL GRAPHIC_3D_SPHERE_URL = getYoGraphicResource("sphere.stl");
   public static final URL GRAPHIC_3D_CUBE_URL = getYoGraphicResource("cube.stl");
   public static final URL GRAPHIC_3D_TETRAHEDRON_URL = getYoGraphicResource("tetrahedron.stl");
   public static final URL GRAPHIC_3D_ICOSAHEDRON_URL = getYoGraphicResource("icosahedron.stl");
   public static final Path GRAPHIC_3D_CUSTOM_GRAPHICS = Paths.get(System.getProperty("user.home"), "scs2", "yoGraphic");
   // YoSliderboard resources:
   public static final URL YO_SLIDERBOARD_BCF2000_WINDOW_URL = getFXMLResource(YO_SLIDERBOARD_BCF2000, "YoBCF2000SliderboardWindow");

   public static final URL MAIN_WINDOW_URL = getFXMLResource("MainWindow");
   public static final URL SECONDARY_WINDOW_URL = getFXMLResource("SecondaryWindow");

   public static final URL SIDE_PANE_URL = getFXMLResource("SidePane");

   public static final URL CHART_PANEL_FXML_URL = getFXMLResource(CHART, "YoChartPanel");
   public static final URL CHART_GROUP_PANEL_URL = getFXMLResource(CHART, "YoChartGroupPanel");
   public static final URL CHART_GROUP_MODEL_EDITOR_PANE_URL = getFXMLResource(CHART, "YoChartGroupModelEditorPane");
   public static final URL CHART_IDENTIFIER_EDITOR_PANE_URL = getFXMLResource(CHART, "YoChartIdentifierEditorPane");
   public static final URL CHART_OPTION_DIALOG_URL = getFXMLResource(CHART, "YoChartOptionDialog");
   public static final URL CHART_VARIABLE_OPTION_PANE_URL = getFXMLResource(CHART, "YoChartVariableOptionPane");

   public static final URL YO_GRAPHIC_ITEM_CREATOR_URL = getFXMLResource(YO_GRAPHIC, "YoGraphicItemCreatorDialog");
   public static final URL YO_GRAPHIC_PROPERTY_URL = getFXMLResource(YO_GRAPHIC, "YoGraphicPropertyWindow");
   public static final URL YO_COMPOSITE_EDITOR_URL = getFXMLResource(YO_GRAPHIC, "YoCompositeEditorPane");

   // Session resources:
   public static final URL REMOTE_SESSION_MANAGER_PANE_FXML_URL = getFXMLResource(SESSION_FOLDER, "RemoteSessionManagerPane");
   public static final URL REMOTE_SESSION_INFO_PANE_FXML_URL = getFXMLResource(SESSION_FOLDER, "YoClientInformationPane");
   public static final URL LOG_SESSION_MANAGER_PANE_FXML_URL = getFXMLResource(SESSION_FOLDER, "LogSessionManagerPane");
   public static final URL LOG_CROP_PROGRESS_PANE_FXML_URL = getFXMLResource(SESSION_FOLDER, "LogCropProgressPane");

   public static URL getCSSResource(String filename)
   {
      return classLoader.getResource(CSS_FOLDER + filename + ".css");
   }

   public static URL getFXMLResource(String filename)
   {
      return classLoader.getResource(FXML_FOLDER + filename + ".fxml");
   }

   public static URL getFXMLResource(String location, String filename)
   {
      return classLoader.getResource(FXML_FOLDER + location + filename + ".fxml");
   }

   public static FXMLLoader getYoGraphicFXEditorFXMLLoader(Class<?> yoGraphicFXType)
   {
      URL fxmlResource = getFXMLResource(YO_GRAPHIC, yoGraphicFXType.getSimpleName() + "EditorPane");
      Objects.requireNonNull(fxmlResource, "Could not find FXML resource for " + yoGraphicFXType.getSimpleName());
      return new FXMLLoader(fxmlResource);
   }

   public static InputStream getIconResource(String iconNameWithExtension)
   {
      return classLoader.getResourceAsStream(ICONS_FOLDER + iconNameWithExtension);
   }

   public static InputStream getImageResource(String imageNameWithExtension)
   {
      return classLoader.getResourceAsStream(IMAGES_FOLDER + imageNameWithExtension);
   }

   public static InputStream getSkyboxResource(String skyboxNameWithExtension)
   {
      return classLoader.getResourceAsStream(SKYBOX_FOLDER + skyboxNameWithExtension);
   }

   public static InputStream getConfigurationResource(String filenameWithExtension)
   {
      return classLoader.getResourceAsStream(CONFIGURATION_FOLDER + filenameWithExtension);
   }

   public static URL getYoGraphicResource(String filenameWithExtension)
   {
      return classLoader.getResource(YO_GRAPHIC_FOLDER + filenameWithExtension);
   }

   public static URL getYoGraphicResource(String location, String filenameWithExtension)
   {
      return classLoader.getResource(YO_GRAPHIC_FOLDER + location + filenameWithExtension);
   }

   public static Image loadIcon(String iconNameWithExtension)
   {
      return new Image(getIconResource(iconNameWithExtension));
   }

   public static Image loadImage(String imageNameWithExtension)
   {
      return new Image(getImageResource(imageNameWithExtension));
   }

   public static File yoChartConfigurationOpenFileDialog(Window owner)
   {
      return showOpenDialog(owner, "Load YoChartGroup", yoChartGroupConfigurationFilter);
   }

   public static File yoChartConfigurationSaveFileDialog(Window owner)
   {
      return showSaveDialog(owner, "Save YoChartGroup", yoChartGroupConfigurationFilter);
   }

   public static File yoGraphicConfigurationOpenFileDialog(Window owner)
   {
      return showOpenDialog(owner, "Load YoGraphic", yoGraphicConfigurationFilter);
   }

   public static File yoGraphicConfigurationSaveFileDialog(Window owner)
   {
      return showSaveDialog(owner, "Save YoGraphic", yoGraphicConfigurationFilter);
   }

   public static File yoCompositeConfigurationOpenFileDialog(Window owner)
   {
      return showOpenDialog(owner, "Load YoComposite", yoCompositeConfigurationFilter);
   }

   public static File yoCompositeConfigurationSaveFileDialog(Window owner)
   {
      return showSaveDialog(owner, "Save YoComposite", yoCompositeConfigurationFilter);
   }

   public static File yoEntryConfigurationOpenFileDialog(Window owner)
   {
      return showOpenDialog(owner, "Load YoEntry", yoEntryConfigurationFilter);
   }

   public static File yoEntryConfigurationSaveFileDialog(Window owner)
   {
      return showSaveDialog(owner, "Save YoEntry", yoEntryConfigurationFilter);
   }

   public static File yoSliderboardConfigurationOpenFileDialog(Window owner)
   {
      return showOpenDialog(owner, "Load YoSliderboard", yoSliderboardConfigurationFilter);
   }

   public static File yoSliderboardConfigurationSaveFileDialog(Window owner)
   {
      return showSaveDialog(owner, "Save YoSliderboard", yoSliderboardConfigurationFilter);
   }

   private static File showSaveDialog(Window owner, String title, ExtensionFilter extensionFilter)
   {
      FileChooser fileChooser = fileChooser(title, extensionFilter);
      File result = fileChooser.showSaveDialog(owner);
      if (result != null)
         setDefaultFilePath(result);
      return result;
   }

   private static File showOpenDialog(Window owner, String title, ExtensionFilter extensionFilter)
   {
      FileChooser fileChooser = fileChooser(title, extensionFilter);
      File result = fileChooser.showOpenDialog(owner);
      if (result != null)
         setDefaultFilePath(result);
      return result;
   }

   private static FileChooser fileChooser(String title, ExtensionFilter extensionFilter)
   {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle(title);
      fileChooser.setInitialDirectory(getDefaultFilePath());
      fileChooser.getExtensionFilters().add(extensionFilter);
      return fileChooser;
   }

   /**
    * Returns the file that was last opened or saved to.
    *
    * @return the most-recently-used file.
    */
   public static File getDefaultFilePath()
   {
      return getDefaultFilePath("filePath");
   }

   public static File getDefaultFilePath(String key)
   {
      Preferences prefs = Preferences.userNodeForPackage(SessionVisualizerIOTools.class);
      String filePath = prefs.get(key, null);

      if (filePath != null && Files.isDirectory(Paths.get(filePath)))
         return new File(filePath);
      else
         return null;
   }

   /**
    * Stores the given file's path as the most-recently-used path. The path is persisted across program
    * runs.
    *
    * @param file the file
    */
   public static void setDefaultFilePath(File file)
   {
      setDefaultFilePath("filePath", file);
   }

   public static void setDefaultFilePath(String key, File file)
   {
      Preferences prefs = Preferences.userNodeForPackage(SessionVisualizerIOTools.class);
      if (file != null)
      {
         if (!file.isDirectory())
            file = file.getParentFile();

         prefs.put(key, file.getAbsolutePath());
      }
   }
}
