package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.io.IOException;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.SecondaryWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.bcf2000.YoBCF2000SliderboardWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.pattern.YoCompositePatternPropertyWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.YoGraphicPropertyWindowController;

public class WindowManager implements Manager
{
   public static final String SECONDARY_CHART_WINDOW_TYPE = "SecondaryChartWindow";
   public static final String COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE = "YoCompositeEditorWindow";
   public static final String GRAPHIC_EDITOR_WINDOW_TYPE = "YoGraphicEditorWindow";
   public static final String BCF2000_SLIDERBOARD_WINDOW_TYPE = "YoGraphicEditorWindow";

   private final SessionVisualizerToolkit toolkit;

   private final Property<YoCompositePatternPropertyWindowController> yoCompositeEditor = new SimpleObjectProperty<>(this, "yoCompositeEditor", null);
   private final Property<YoGraphicPropertyWindowController> yoGraphicEditor = new SimpleObjectProperty<>(this, "yoGraphicEditor", null);
   private final Property<YoBCF2000SliderboardWindowController> bcf2000Sliderboard = new SimpleObjectProperty<>(this, "bcf2000Sliderboard", null);

   public WindowManager(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      JavaFXMessager messager = toolkit.getMessager();
      SessionVisualizerTopics topics = toolkit.getTopics();

      messager.registerTopicListener(topics.getOpenWindowRequest(), this::openWindow);
   }

   @Override
   public boolean isSessionLoaded()
   {
      return false;
   }

   @Override
   public void startSession(Session session)
   {
   }

   @Override
   public void stopSession()
   {
      if (yoCompositeEditor.getValue() != null)
      {
         yoCompositeEditor.getValue().close();
         yoCompositeEditor.setValue(null);
      }
      if (yoGraphicEditor.getValue() != null)
      {
         yoGraphicEditor.getValue().close();
         yoGraphicEditor.setValue(null);
      }
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().close();
         bcf2000Sliderboard.setValue(null);
      }
   }

   public void openWindow(String windowType)
   {
      if (windowType == null || windowType.isEmpty())
         return;

      switch (windowType)
      {
         case COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE:
            openYoCompositePatternEditor();
            break;
         case SECONDARY_CHART_WINDOW_TYPE:
            newChartWindow();
            break;
         case GRAPHIC_EDITOR_WINDOW_TYPE:
            openYoGraphicEditor();
            break;
         default:
            LogTools.error("Unexpected value: " + windowType);
      }
   }

   public void openYoCompositePatternEditor()
   {
      if (yoCompositeEditor.getValue() != null)
      {
         yoCompositeEditor.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_PATTERN_PROPERTY_WINDOW_URL);
         fxmlLoader.load();
         YoCompositePatternPropertyWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         yoCompositeEditor.setValue(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void newChartWindow()
   {
      Stage newWindow = WindowManager.newSecondaryChartWindow(toolkit);
      newWindow.show();
   }

   public static Stage newSecondaryChartWindow(SessionVisualizerToolkit toolkit)
   {
      Stage stage = new Stage();

      try
      {
         // Loading template for secondary window
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.SECONDARY_WINDOW_URL);
         loader.load();
         SecondaryWindowController controller = loader.getController();
         controller.initialize(toolkit, stage);
         controller.setupChartGroup();
         controller.start();
         return stage;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         stage.close();
         return null;
      }
   }

   private void openYoGraphicEditor()
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
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void openBCF2000SliderboardWindow()
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_SLIDERBOARD_BCF2000_WINDOW_URL);
         fxmlLoader.load();
         YoBCF2000SliderboardWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         bcf2000Sliderboard.setValue(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
