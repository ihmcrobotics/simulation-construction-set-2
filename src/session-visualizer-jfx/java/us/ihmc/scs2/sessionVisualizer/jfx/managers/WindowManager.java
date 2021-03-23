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
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.pattern.YoCompositePatternPropertyWindowController;

public class WindowManager implements Manager
{
   public static final String SECONDARY_CHART_WINDOW_TYPE = "SecondaryChartWindow";
   public static final String COMPOSITE_PATTERN_EDITOR_WINDOW_TYPE = "YoCompositeEditorWindow";
   public static final String GRAPHIC_EDITOR_WINDOW_TYPE = "YoCompositeEditorWindow";

   private final SessionVisualizerToolkit toolkit;

   private final Property<YoCompositePatternPropertyWindowController> activeYoCompositeEditController = new SimpleObjectProperty<>(this,
                                                                                                                                   "activeYoCompositeEditController",
                                                                                                                                   null);

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
         default:
            LogTools.error("Unexpected value: " + windowType);
      }
   }

   public void openYoCompositePatternEditor()
   {
      if (activeYoCompositeEditController.getValue() != null)
      {
         activeYoCompositeEditController.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_PATTERN_PROPERTY_WINDOW_URL);
         fxmlLoader.load();
         YoCompositePatternPropertyWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         activeYoCompositeEditController.setValue(controller);
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
}
