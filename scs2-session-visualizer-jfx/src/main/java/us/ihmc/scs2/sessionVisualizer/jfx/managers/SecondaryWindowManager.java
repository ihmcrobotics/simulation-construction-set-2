package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import us.ihmc.log.LogTools;
import us.ihmc.messager.SynchronizeHint;
import us.ihmc.messager.javafx.JavaFXMessager;
import us.ihmc.scs2.definition.configuration.WindowConfigurationDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardType;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.SCSGuiConfiguration;
import us.ihmc.scs2.sessionVisualizer.jfx.SecondaryWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.YoRegistryStatisticsPaneController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.YoSliderboardManager;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.creator.YoCompositeAndEquationEditorWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.pattern.YoCompositePatternPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.yoVariables.registry.YoNamespace;
import us.ihmc.yoVariables.registry.YoRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecondaryWindowManager implements Manager
{
   private static final double SECONDARY_WINDOW_POSITION_OFFSET = 30.0;

   private final SessionVisualizerToolkit toolkit;

   private final Property<YoCompositePatternPropertyWindowController> yoCompositePatternEditor = new SimpleObjectProperty<>(this,
                                                                                                                            "yoCompositePatternEditor",
                                                                                                                            null);
   private final Property<YoCompositeAndEquationEditorWindowController> yoCompositeCreator = new SimpleObjectProperty<>(this, "yoCompositeCreator", null);
   private final Property<YoGraphicPropertyWindowController> yoGraphicEditor = new SimpleObjectProperty<>(this, "yoGraphicEditor", null);
   private final Property<YoRegistryStatisticsPaneController> yoRegistryStatistics = new SimpleObjectProperty<>(this, "yoRegistryStatistics", null);
   private final List<Stage> secondaryWindows = new ArrayList<>();
   private final List<SecondaryWindowController> secondaryWindowControllers = new ArrayList<>();
   private final YoSliderboardManager sliderboardManager;
   private final JavaFXMessager messager;
   private final SessionVisualizerTopics topics;

   public SecondaryWindowManager(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      messager = toolkit.getMessager();
      topics = toolkit.getTopics();

      sliderboardManager = new YoSliderboardManager(toolkit);

      messager.addTopicListener(topics.getOpenWindowRequest(), this::openWindow);
   }

   @Override
   public boolean isSessionLoaded()
   {
      // TODO Should probably return something else here and use it.
      return false;
   }

   @Override
   public void startSession(Session session)
   {
      sliderboardManager.startSession(session);
   }

   public void loadSessionConfiguration(SCSGuiConfiguration configuration)
   {
      JavaFXMissingTools.runAndWait(getClass(), () ->
      {
         if (configuration.hasSecondaryWindowConfigurations())
         {
            List<WindowConfigurationDefinition> secondaryWindowConfigurations = configuration.getSecondaryWindowConfigurations();

            for (int i = 0; i < configuration.getNumberOfSecondaryYoChartGroupConfigurations(); i++)
            {
               WindowConfigurationDefinition secondaryWindowConfiguration = secondaryWindowConfigurations.get(i);
               Stage stage = newChartWindow(null, secondaryWindowConfiguration);
               messager.submitMessage(topics.getYoChartGroupLoadConfiguration(),
                                      new Pair<>(stage, configuration.getSecondaryYoChartGroupConfigurationFile(i)),
                                      SynchronizeHint.SYNCHRONOUS);
            }
         }
      });
   }

   public void saveSessionConfiguration(SCSGuiConfiguration configuration)
   {
      sliderboardManager.saveSessionConfiguration(configuration);

      for (SecondaryWindowController secondaryWindow : secondaryWindowControllers)
      {
         secondaryWindow.saveSessionConfiguration(configuration);
      }
   }

   @Override
   public void stopSession()
   {
      if (yoCompositePatternEditor.getValue() != null)
      {
         yoCompositePatternEditor.getValue().closeAndDispose();
         yoCompositePatternEditor.setValue(null);
      }

      if (yoGraphicEditor.getValue() != null)
      {
         yoGraphicEditor.getValue().closeAndDispose();
         yoGraphicEditor.setValue(null);
      }

      if (yoCompositeCreator.getValue() != null)
      {
         yoCompositeCreator.getValue().closeAndDispose();
         yoCompositeCreator.setValue(null);
      }

      sliderboardManager.stopSession();

      if (yoRegistryStatistics.getValue() != null)
      {
         yoRegistryStatistics.getValue().close();
         yoRegistryStatistics.setValue(null);
      }

      closeAllSecondaryWindows();
   }

   public void closeAllSecondaryWindows()
   {
      secondaryWindows.forEach(secondaryWindow -> secondaryWindow.fireEvent(new WindowEvent(secondaryWindow, WindowEvent.WINDOW_CLOSE_REQUEST)));
      secondaryWindowControllers.forEach(SecondaryWindowController::closeAndDispose);
      secondaryWindowControllers.clear();
   }

   public void openWindow(NewWindowRequest request)
   {
      if (request == null)
         return;

      switch (request.windowType)
      {
         case NewWindowRequest.COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE:
            openYoCompositePatternEditor(request.requestSource);
            break;
         case NewWindowRequest.COMPOSITE_CREATOR_WINDOW_TYPE:
            openYoCompositeCreator(request.requestSource);
            break;
         case NewWindowRequest.SECONDARY_CHART_WINDOW_TYPE:
            newChartWindow(request.requestSource);
            break;
         case NewWindowRequest.GRAPHIC_EDITOR_WINDOW_TYPE:
            openYoGraphicEditor(request.requestSource);
            break;
         case NewWindowRequest.BFC2000_SLIDERBOARD_WINDOW_TYPE:
            sliderboardManager.openSliderboardWindow(request.requestSource, YoSliderboardType.BCF2000);
            return;
         case NewWindowRequest.XTOUCHCOMPACT_SLIDERBOARD_WINDOW_TYPE:
            sliderboardManager.openSliderboardWindow(request.requestSource, YoSliderboardType.XTOUCHCOMPACT);
            return;
         case NewWindowRequest.REGISTRY_STATISTICS_WINDOW_TYPE:
            openRegistryStatisticsWindow(request.requestSource, (String) request.additionalData);
         default:
            LogTools.error("Unexpected value: " + request.windowType);
      }
   }

   private void openYoCompositeCreator(Window requestSource)
   {
      if (yoCompositeCreator.getValue() != null)
      {
         yoCompositeCreator.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_AND_EQUATION_EDITOR_WINDOW_URL);
         fxmlLoader.load();
         YoCompositeAndEquationEditorWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         yoCompositeCreator.setValue(controller);
         initializeSecondaryWindowWithOwner(requestSource, controller.getWindow());
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void openYoCompositePatternEditor(Window requestSource)
   {
      if (yoCompositePatternEditor.getValue() != null)
      {
         yoCompositePatternEditor.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_PATTERN_PROPERTY_WINDOW_URL);
         fxmlLoader.load();
         YoCompositePatternPropertyWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         yoCompositePatternEditor.setValue(controller);
         initializeSecondaryWindowWithOwner(requestSource, controller.getWindow());
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public Stage newChartWindow(Window requestSource)
   {
      return newChartWindow(requestSource, null);
   }

   public Stage newChartWindow(Window requestSource, WindowConfigurationDefinition windowConfigurationDefinition)
   {
      Stage stage = newStage(requestSource, windowConfigurationDefinition);

      try
      {
         // Loading template for secondary window
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.SECONDARY_WINDOW_URL);
         loader.load();
         SecondaryWindowController controller = loader.getController();
         controller.initialize(new SessionVisualizerWindowToolkit(stage, toolkit));
         secondaryWindowControllers.add(controller);
         stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
         {
            if (!e.isConsumed())
               secondaryWindowControllers.remove(controller);
         });
         controller.start();
         stage.show();
         return stage;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         stage.close();
         return null;
      }
   }

   private Stage newStage(Window requestSource, WindowConfigurationDefinition definition)
   {
      Stage stage = new Stage();

      if (definition != null)
         SCSGuiConfiguration.loadWindowConfigurationDefinition(definition, stage);
      else
         initializeSecondaryWindowWithOwner(requestSource, stage);

      secondaryWindows.add(stage);
      return stage;
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
         definition.setShowing(stage.isShowing());
      }
      else
      {
         definition.setMaximized(false);
         definition.setPositionX(stage.getX());
         definition.setPositionY(stage.getY());
         definition.setWidth(stage.getWidth());
         definition.setHeight(stage.getHeight());
         definition.setShowing(stage.isShowing());
      }
      return definition;
   }

   private void openYoGraphicEditor(Window requestSource)
   {
      if (yoGraphicEditor.getValue() != null)
      {
         yoGraphicEditor.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_GRAPHIC_PROPERTY_URL);
         fxmlLoader.load();
         YoGraphicPropertyWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         yoGraphicEditor.setValue(controller);
         initializeSecondaryWindowWithOwner(requestSource, controller.getWindow());
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void openRegistryStatisticsWindow(Window requestSource, String registryFullname)
   {
      YoRegistry registry = toolkit.getYoManager().getRootRegistry().findRegistry(new YoNamespace(registryFullname));

      if (registry == null)
      {
         LogTools.error("Could not find a registry from the name: {}", registryFullname);
         return;
      }

      if (yoRegistryStatistics.getValue() != null)
      {
         yoRegistryStatistics.getValue().setInput(registry);
         yoRegistryStatistics.getValue().showWindow();
      }
      else
      {
         try
         {
            FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_REGISTRY_STATISTICS_URL);
            fxmlLoader.load();
            YoRegistryStatisticsPaneController controller = fxmlLoader.getController();
            controller.initialize(toolkit);
            controller.setInput(registry);
            yoRegistryStatistics.setValue(controller);
            initializeSecondaryWindowWithOwner(requestSource, controller.getWindow());
            controller.showWindow();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }

   public static void initializeSecondaryWindowWithOwner(Window owner, Stage secondary)
   {
      if (owner == null)
         return;
      secondary.setX(owner.getX() + SECONDARY_WINDOW_POSITION_OFFSET);
      secondary.setY(owner.getY() + 30.0);
   }

   public int getNumberOfSecondaryWindows()
   {
      return secondaryWindows.size();
   }

   public SecondaryWindowController getSecondaryWindowController(int index)
   {
      return secondaryWindowControllers.get(index);
   }

   public static class NewWindowRequest
   {
      public static final String REGISTRY_STATISTICS_WINDOW_TYPE = "YoRegistryStatisticsWindow";
      public static final String BFC2000_SLIDERBOARD_WINDOW_TYPE = "BFC2000EditorWindow";
      public static final String XTOUCHCOMPACT_SLIDERBOARD_WINDOW_TYPE = "XTouchCompactEditorWindow";
      public static final String GRAPHIC_EDITOR_WINDOW_TYPE = "YoGraphicEditorWindow";
      public static final String COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE = "YoCompositePatternEditorWindow";
      public static final String COMPOSITE_CREATOR_WINDOW_TYPE = "YoCompositeEditorWindow";
      public static final String SECONDARY_CHART_WINDOW_TYPE = "SecondaryChartWindow";

      private final String windowType;
      private final Window requestSource;
      private final Object additionalData;

      public NewWindowRequest(String windowType, Window requestSource)
      {
         this(windowType, requestSource, null);
      }

      public NewWindowRequest(String windowType, Window requestSource, Object additionalData)
      {
         this.windowType = windowType;
         this.requestSource = requestSource;
         this.additionalData = additionalData;
      }

      public static NewWindowRequest registryStatisticWindow(Window requestSource, YoRegistry registry)
      {
         return new NewWindowRequest(REGISTRY_STATISTICS_WINDOW_TYPE, requestSource, registry.getNamespace().toString());
      }

      public static NewWindowRequest bfc2000SliderboardWindow(Window requestSource)
      {
         return new NewWindowRequest(BFC2000_SLIDERBOARD_WINDOW_TYPE, requestSource);
      }

      public static NewWindowRequest xtouchCompactSliderboardWindow(Window requestSource)
      {
         return new NewWindowRequest(XTOUCHCOMPACT_SLIDERBOARD_WINDOW_TYPE, requestSource);
      }

      public static NewWindowRequest graphicEditorWindow(Window requestSource)
      {
         return new NewWindowRequest(GRAPHIC_EDITOR_WINDOW_TYPE, requestSource);
      }

      public static NewWindowRequest compositePatternEditorWindow(Window requestSource)
      {
         return new NewWindowRequest(COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE, requestSource);
      }

      public static NewWindowRequest compositeCreatorWindow(Window requestSource)
      {
         return new NewWindowRequest(COMPOSITE_CREATOR_WINDOW_TYPE, requestSource);
      }

      public static NewWindowRequest chartWindow(Window requestSource)
      {
         return new NewWindowRequest(SECONDARY_CHART_WINDOW_TYPE, requestSource);
      }
   }
}
