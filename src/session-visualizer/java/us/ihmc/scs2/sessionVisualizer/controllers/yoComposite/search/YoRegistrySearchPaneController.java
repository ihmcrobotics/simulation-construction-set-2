package us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.search;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.BackgroundExecutorManager;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.tools.TreeViewTools;
import us.ihmc.scs2.sessionVisualizer.tools.YoVariableTools;
import us.ihmc.yoVariables.registry.YoRegistry;

public class YoRegistrySearchPaneController extends AnimationTimer
{
   @FXML
   private TextField searchTextField;

   @FXML
   private TreeView<YoRegistry> registryTreeView;

   private List<YoRegistry> allRegistries;

   private TreeItem<YoRegistry> defaultRootItem;
   private TreeItem<YoRegistry> searchResult = null;
   private boolean showRoot = true;

   private AtomicReference<SearchEngines> activeSearchEngine;

   private YoRegistry rootRegistry;

   private Future<TreeItem<YoRegistry>> backgroundSearch;

   private boolean refreshRootRegistry;

   private YoManager yoManager;
   private BackgroundExecutorManager backgroundExecutorManager;

   private Consumer<YoRegistry> registryViewRequestConsumer = null;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      yoManager = toolkit.getYoManager();
      backgroundExecutorManager = toolkit.getBackgroundExecutorManager();
      registryTreeView.setCellFactory(param -> new YoRegistryTreeCell());
      registryTreeView.setRoot(defaultRootItem);
      yoManager.rootRegistryHashCodeProperty().addListener((o, oldValue, newValue) -> refreshRootRegistry = true);

      searchTextField.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> search(newValue));

      JavaFXMessager messager = toolkit.getMessager();
      SessionVisualizerTopics topics = toolkit.getTopics();
      activeSearchEngine = messager.createInput(topics.getYoSearchEngine(), SearchEngines.DEFAULT);

      messager.registerJavaFXSyncedTopicListener(topics.getSessionCurrentState(), state ->
      {
         if (state == SessionState.ACTIVE)
         {
            refreshRootRegistry = true;
            start();
         }
         else if (state == SessionState.INACTIVE)
         {
            stop();
            searchResult = null;
            allRegistries = Collections.emptyList();
            defaultRootItem = new TreeItem<>();
            registryTreeView.setRoot(defaultRootItem);
            searchTextField.clear();
         }
      });
   }

   public void setRegistryViewRequestConsumer(Consumer<YoRegistry> consumer)
   {
      this.registryViewRequestConsumer = consumer;
   }

   private void refreshRootRegistry()
   {
      rootRegistry = yoManager.getRootRegistry();

      if (rootRegistry == null)
      {
         refreshRootRegistry = true;
         allRegistries = null;
         defaultRootItem = null;
      }
      else
      {
         allRegistries = rootRegistry.collectSubtreeRegistries();
         defaultRootItem = new TreeItem<>(rootRegistry);
         buildTreeRecursively(defaultRootItem);
      }
   }

   @Override
   public void handle(long now)
   {
      if (backgroundSearch != null && backgroundSearch.isDone() && !backgroundSearch.isCancelled())
      {
         try
         {
            searchResult = backgroundSearch.get();
         }
         catch (InterruptedException | ExecutionException e)
         {
            e.printStackTrace();
         }
      }

      if (searchResult != null)
      {
         registryTreeView.setShowRoot(showRoot);
         registryTreeView.setRoot(searchResult);
         searchResult = null;
      }

      if (refreshRootRegistry)
      {
         refreshRootRegistry = false;
         refreshRootRegistry();
         if (!refreshRootRegistry)
            search(searchTextField.getText());
      }
   }

   private void search(String searchQuery)
   {
      if (backgroundSearch != null)
      {
         backgroundSearch.cancel(true);
         backgroundSearch = null;
      }

      if (searchQuery != null && !searchQuery.isEmpty())
      {
         backgroundSearch = backgroundExecutorManager.executeInBackground(() ->
         {
            if (allRegistries == null)
            {
               refreshRootRegistry = true;
               return null;
            }

            return createRootItemForRegistries(YoVariableTools.search(allRegistries,
                                                                      YoRegistry::getName,
                                                                      searchQuery,
                                                                      YoVariableTools.fromSearchEnginesEnum(activeSearchEngine.get()),
                                                                      Integer.MAX_VALUE,
                                                                      Collectors.toSet()));
         });
      }
      else
      {
         searchResult = defaultRootItem;
         showRoot = false;
      }
   }

   @FXML
   void openRegistryTab(MouseEvent event)
   {
      if (registryViewRequestConsumer == null)
         return;

      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
      {
         TreeItem<YoRegistry> selectedItem = registryTreeView.getSelectionModel().getSelectedItem();

         if (selectedItem != null)
            registryViewRequestConsumer.accept(selectedItem.getValue());

         event.consume();
      }
   }

   private TreeItem<YoRegistry> createRootItemForRegistries(Set<YoRegistry> subSelection)
   {
      TreeItem<YoRegistry> root = new TreeItem<>(rootRegistry);
      buildTreeRecursively(root);
      filterRegistries(root, subSelection);
      TreeViewTools.expandRecursively(root);
      return root;
   }

   private static void filterRegistries(TreeItem<YoRegistry> parent, Set<YoRegistry> registriesToKeep)
   {
      if (parent == null || parent.isLeaf())
         return;

      for (TreeItem<YoRegistry> child : parent.getChildren())
      {
         if (!child.getChildren().isEmpty())
            filterRegistries(child, registriesToKeep);
      }

      for (int i = parent.getChildren().size() - 1; i >= 0; i--)
      {
         TreeItem<YoRegistry> child = parent.getChildren().get(i);

         if (child.isLeaf() && !registriesToKeep.contains(child.getValue()))
            parent.getChildren().remove(i);
      }
   }

   private static void buildTreeRecursively(TreeItem<YoRegistry> parent)
   {
      for (YoRegistry child : parent.getValue().getChildren())
      {
         TreeItem<YoRegistry> childItem = new TreeItem<>(child);
         parent.getChildren().add(childItem);
         buildTreeRecursively(childItem);
      }
   }
}
