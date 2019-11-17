package us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.bind.JAXBException;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoEntry.YoEntryConfigurationDefinition;
import us.ihmc.scs2.definition.yoEntry.YoEntryListDefinition;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.tools.ContextMenuTools;
import us.ihmc.scs2.sessionVisualizer.tools.TabPaneTools;
import us.ihmc.scs2.sessionVisualizer.xml.XMLTools;

public class YoEntryTabPaneController
{
   @FXML
   private TabPane yoEntryTabPane;
   @FXML
   private Tab initialTab;
   @FXML
   private YoEntryListViewController initialListViewController;

   private final Map<Tab, YoEntryListViewController> tabToControllerMap = new HashMap<>();

   private SessionVisualizerToolkit toolkit;
   private Window owner;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      owner = toolkit.getMainWindow();
      initialListViewController.initialize(toolkit);
      Label initialTabHeader = TabPaneTools.editableTabHeader(initialTab);
      initialListViewController.nameProperty().bindBidirectional(initialTabHeader.textProperty());

      tabToControllerMap.put(initialTab, initialListViewController);
      ContextMenuTools.setupContextMenu(yoEntryTabPane,
                                        TabPaneTools.addBeforeMenuItemFactory(this::newEmptyTab),
                                        TabPaneTools.addAfterMenuItemFactory(this::newEmptyTab),
                                        TabPaneTools.removeAllMenuItemFactory("Clear all", true),
                                        exportTabMenuItemFactory(),
                                        exportAllTabMenuItemFactory(),
                                        importTabMenuItemFactory());

      yoEntryTabPane.getTabs().addListener((ListChangeListener<Tab>) change ->
      {
         while (change.next())
         {
            if (change.wasRemoved())
            {
               for (Tab removedTab : change.getRemoved())
               {
                  if (removedTab == initialTab)
                  {
                     Platform.runLater(() -> yoEntryTabPane.getTabs().add(initialTab));
                     initialTabHeader.setText("Default");
                     initialListViewController.clear();
                  }
                  else
                  {
                     tabToControllerMap.remove(removedTab);
                  }
               }
            }

            if (change.getRemoved().contains(initialTab))
            {

            }
         }
      });

      toolkit.getMessager().registerJavaFXSyncedTopicListener(toolkit.getTopics().getSessionCurrentState(), state ->
      {
         if (state == SessionState.INACTIVE)
            yoEntryTabPane.getTabs().clear();
      });
   }

   public void setInput(YoEntryConfigurationDefinition input)
   {
      if (input.getYoEntryLists() == null)
         return;

      ObservableList<Tab> tabs = yoEntryTabPane.getTabs();
      tabs.retainAll(initialTab);

      while (tabs.size() < input.getYoEntryLists().size())
         tabs.add(newEmptyTab());

      List<YoEntryListDefinition> yoEntryLists = input.getYoEntryLists();
      for (int i = 0; i < yoEntryLists.size(); i++)
      {
         setTabInput(tabs.get(i), yoEntryLists.get(i));
      }
   }

   private void setTabInput(Tab tab, YoEntryListDefinition input)
   {
      tabToControllerMap.get(tab).setInput(input);
   }

   public YoEntryConfigurationDefinition toYoEntryConfigurationDefinition()
   {
      YoEntryConfigurationDefinition definition = new YoEntryConfigurationDefinition();
      definition.setYoEntryLists(new ArrayList<>());
      for (Tab tab : yoEntryTabPane.getTabs())
      {
         definition.getYoEntryLists().add(tabToControllerMap.get(tab).toYoEntryListDefinition());
      }
      return definition;
   }

   private Function<TabPane, MenuItem> exportTabMenuItemFactory()
   {
      return tabPane ->
      {
         Tab selectedTab = yoEntryTabPane.getSelectionModel().getSelectedItem();

         if (selectedTab == null)
            return null;

         FontAwesomeIconView exportIcon = new FontAwesomeIconView();
         exportIcon.getStyleClass().add("save-icon-view");
         MenuItem menuItem = new MenuItem("Export active tab...", exportIcon);

         menuItem.setOnAction(e ->
         {
            File result = SessionVisualizerIOTools.yoEntryConfigurationSaveFileDialog(owner);
            if (result != null)
               exportSingleTab(result, selectedTab);
         });

         return menuItem;
      };
   }

   private Function<TabPane, MenuItem> exportAllTabMenuItemFactory()
   {
      return tabPane ->
      {
         Tab selectedTab = yoEntryTabPane.getSelectionModel().getSelectedItem();

         if (selectedTab == null)
            return null;

         FontAwesomeIconView exportIcon = new FontAwesomeIconView();
         exportIcon.getStyleClass().add("save-icon-view");
         MenuItem menuItem = new MenuItem("Export all tabs...", exportIcon);

         menuItem.setOnAction(e ->
         {
            File result = SessionVisualizerIOTools.yoEntryConfigurationSaveFileDialog(owner);
            if (result != null)
               exportAllTabs(result);
         });

         return menuItem;
      };
   }

   private Function<TabPane, MenuItem> importTabMenuItemFactory()
   {
      return tabPane ->
      {
         Tab selectedTab = yoEntryTabPane.getSelectionModel().getSelectedItem();

         if (selectedTab == null)
            return null;

         FontAwesomeIconView exportIcon = new FontAwesomeIconView();
         exportIcon.getStyleClass().add("load-icon-view");
         MenuItem menuItem = new MenuItem("Import tab(s)...", exportIcon);

         menuItem.setOnAction(e ->
         {
            File result = SessionVisualizerIOTools.yoEntryConfigurationOpenFileDialog(owner);
            if (result != null)
               importTabsAt(result, selectedTab);
         });

         return menuItem;
      };
   }

   private Tab newEmptyTab()
   {
      try
      {
         Tab tab = new Tab("Tab" + (yoEntryTabPane.getTabs().size() - 1));
         Label tabHeader = TabPaneTools.editableTabHeader(tab);
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.YO_ENTRY_LIST_VIEW_URL);
         Node node = loader.load();
         tab.setContent(node);
         YoEntryListViewController controller = loader.getController();
         controller.nameProperty().bindBidirectional(tabHeader.textProperty());
         controller.initialize(toolkit);
         tabToControllerMap.put(tab, controller);
         return tab;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void exportAllTabs(File file)
   {
      LogTools.info("Saving to file: " + file);

      try
      {
         XMLTools.saveYoEntryConfigurationDefinition(new FileOutputStream(file), toYoEntryConfigurationDefinition());
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void exportSingleTab(File file, Tab tabToExport)
   {
      LogTools.info("Saving single tab to file: " + file);

      try
      {
         YoEntryListDefinition tabDefinition = tabToControllerMap.get(tabToExport).toYoEntryListDefinition();
         XMLTools.saveYoEntryConfigurationDefinition(new FileOutputStream(file), new YoEntryConfigurationDefinition(tabDefinition));
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void load(File file)
   {
      LogTools.info("Loading from file: " + file);

      try
      {
         YoEntryConfigurationDefinition definition = XMLTools.loadYoEntryConfigurationDefinition(new FileInputStream(file));
         setInput(definition);
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void importTabsAt(File file, Tab insertionPoint)
   {
      LogTools.info("Loading from file: " + file);

      try
      {
         YoEntryConfigurationDefinition definition = XMLTools.loadYoEntryConfigurationDefinition(new FileInputStream(file));

         List<YoEntryListDefinition> yoEntryLists = definition.getYoEntryLists();
         if (yoEntryLists == null || yoEntryLists.isEmpty())
            return;

         int startIndex = 0;

         if (isTabEmpty(insertionPoint))
         {
            setTabInput(insertionPoint, yoEntryLists.get(0));
            startIndex++;
         }

         ObservableList<Tab> tabs = yoEntryTabPane.getTabs();
         int insertionIndex = tabs.indexOf(insertionPoint) + 1;

         for (int i = startIndex; i < yoEntryLists.size(); i++)
         {
            Tab newEmptyTab = newEmptyTab();
            setTabInput(newEmptyTab, yoEntryLists.get(i));
            tabs.add(insertionIndex, newEmptyTab);
            insertionIndex++;
         }
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   private boolean isTabEmpty(Tab query)
   {
      return tabToControllerMap.get(query).isEmpty();
   }

   public void start()
   {
   }

   public void stop()
   {
   }
}
