package us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoCompositeTools;
import us.ihmc.yoVariables.registry.YoRegistry;

public class YoSearchTabPaneController
{
   @FXML
   private TabPane yoSearchTabPane;
   @FXML
   private YoRegistrySearchPaneController yoRegistrySearchPaneController;
   @FXML
   private YoCompositeSearchPaneController mainYoCompositeSearchPaneController;

   private SessionVisualizerToolkit toolkit;
   private final Map<YoRegistry, Tab> registryTabs = new HashMap<>();

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      yoSearchTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
      yoSearchTabPane.getSelectionModel().select(1);
      yoRegistrySearchPaneController.initialize(toolkit);
      yoRegistrySearchPaneController.setRegistryViewRequestConsumer(newRequest ->
      {
         if (newRequest != null)
            openRegistryTab(newRequest, 1);
      });

      mainYoCompositeSearchPaneController.initialize(toolkit);
      mainYoCompositeSearchPaneController.setSearchTarget(YoCompositeTools.YO_VARIABLE);
      mainYoCompositeSearchPaneController.setRegistryViewRequestConsumer(newRequest ->
      {
         if (newRequest == null)
            return;
         try
         {
            YoRegistry registry = toolkit.getYoManager().getRootRegistry().findRegistry(newRequest);
            openRegistryTab(registry, -1);
         }
         catch (RuntimeException e)
         {
            LogTools.error("Registry not found: " + newRequest);
         }
      });
   }

   public void start()
   {
      yoRegistrySearchPaneController.start();
      mainYoCompositeSearchPaneController.start();
   }

   public void stop()
   {
      yoRegistrySearchPaneController.stop();
      mainYoCompositeSearchPaneController.stop();
   }

   private void openRegistryTab(YoRegistry registry, int tabIndex)
   {
      if (registryTabs.containsKey(registry))
      {
         yoSearchTabPane.getSelectionModel().select(registryTabs.get(registry));
      }
      else
      {
         Tab newTab = new Tab(registry.getName());
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.YO_COMPOSITE_SEARCH_PANEL_URL);
         try
         {
            Pane rootPane = loader.load();
            YoCompositeSearchPaneController controller = loader.getController();
            controller.initialize(toolkit, registry);
            controller.setSearchTarget(mainYoCompositeSearchPaneController.getSearchTarget());
            controller.start();
            registryTabs.put(registry, newTab);
            newTab.setContent(rootPane);
            newTab.setClosable(true);
            newTab.setOnClosed(e ->
            {
               controller.stop();
               registryTabs.remove(registry);
            });
            if (tabIndex == -1)
               yoSearchTabPane.getTabs().add(yoSearchTabPane.getTabs().size() - 1, newTab);
            else
               yoSearchTabPane.getTabs().add(tabIndex, newTab);
            yoSearchTabPane.getSelectionModel().select(newTab);
         }
         catch (IOException e)
         {
            throw new RuntimeException("Could not load the registry tab.", e);
         }
      }
   }

   @FXML
   void startYoVariableDragAndDrop(MouseEvent event)
   {
      mainYoCompositeSearchPaneController.startYoVariableDragAndDrop(event);
   }
}
