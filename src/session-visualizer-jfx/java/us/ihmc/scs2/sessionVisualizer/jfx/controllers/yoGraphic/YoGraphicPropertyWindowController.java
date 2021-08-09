package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.controlsfx.control.CheckTreeView;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFXItem;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGroupFX;

public class YoGraphicPropertyWindowController
{
   @FXML
   private AnchorPane mainAnchorPane;
   @FXML
   private CheckTreeView<YoGraphicFXItem> yoGraphicTreeView;
   @FXML
   private Button addItemButton, removeItemButton;
   @FXML
   private Label yoGraphicTypeLabel;
   @FXML
   private AnchorPane yoGraphicEditorPane;
   @FXML
   private Button saveChangesButton, revertChangesButton;

   private CheckBoxTreeItem<YoGraphicFXItem> rootItem;

   private SessionVisualizerToolkit toolkit;
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;
   private final ObjectProperty<YoGraphicFXCreatorController<YoGraphicFX>> activeEditor = new SimpleObjectProperty<>(this, "activeEditor", null);
   private final Map<YoGraphicFXItem, YoGraphicFXCreatorController<YoGraphicFX>> cachedEditors = new HashMap<>();
   private final ObjectProperty<ContextMenu> activeContexMenu = new SimpleObjectProperty<>(this, "activeContextMenu", null);
   private YoGroupFX rootGroup;
   private YoGroupFX sessionRootGroup;
   private Stage window;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      topics = toolkit.getTopics();
      messager = toolkit.getMessager();
      rootGroup = toolkit.getYoGraphicFXRootGroup();
      sessionRootGroup = toolkit.getYoGraphicFXSessionRootGroup();

      initializeTreeViewAutoRefreshListener(rootGroup);

      yoGraphicTreeView.setCellFactory(param -> new YoGraphicFXItemTreeCell(rootGroup));
      yoGraphicTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      yoGraphicTreeView.getSelectionModel().selectedItemProperty()
                       .addListener((observable, oldValue, newValue) -> processTreeSelectionUpdate(oldValue, newValue));
      yoGraphicTreeView.setShowRoot(true);
      yoGraphicTreeView.setOnDragDetected(this::handleDragDetected);
      yoGraphicTreeView.addEventHandler(KeyEvent.KEY_PRESSED, e ->
      {
         if (e.getCode() == KeyCode.DELETE)
            removeItem();
      });
      yoGraphicTreeView.setOnContextMenuRequested(e ->
      {
         if (activeContexMenu.get() != null)
         {
            activeContexMenu.get().hide();
            activeContexMenu.set(null);
         }

         FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
         FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
         FontAwesomeIconView duplicateIcon = new FontAwesomeIconView(FontAwesomeIcon.CLONE);
         addIcon.setFill(Color.web("#89e0c0"));
         deleteIcon.setFill(Color.web("#edafb7"));
         duplicateIcon.setFill(Color.web("#8996e0"));
         MenuItem addItem = new MenuItem("Add item...", addIcon);
         MenuItem removeItem = new MenuItem("Remove item", deleteIcon);
         MenuItem duplicateItem = new MenuItem("Duplicate item", duplicateIcon);
         addItem.setOnAction(e2 -> addItem());
         removeItem.setOnAction(e2 -> removeItem());
         duplicateItem.setOnAction(e2 -> duplicateItem());

         ContextMenu contextMenu = new ContextMenu(addItem, removeItem, duplicateItem);
         contextMenu.show(yoGraphicTreeView, e.getScreenX(), e.getScreenY());
         activeContexMenu.set(contextMenu);
      });
      refreshTreeView();

      saveChangesButton.setDisable(true);
      revertChangesButton.setDisable(true);

      activeEditor.addListener((observable, oldValue, newValue) ->
      {
         saveChangesButton.disableProperty().unbind();
         revertChangesButton.disableProperty().unbind();

         if (newValue == null)
         {
            saveChangesButton.setDisable(true);
            revertChangesButton.setDisable(true);
         }
         else
         {
            saveChangesButton.disableProperty().bind(newValue.hasChangesPendingProperty().and(newValue.inputsValidityProperty()).not());
            revertChangesButton.disableProperty().bind(newValue.hasChangesPendingProperty().not());
         }
      });

      yoGraphicEditorPane.addEventHandler(KeyEvent.KEY_PRESSED, e ->
      {
         if (e.getCode() == KeyCode.CONTROL)
            return;
         if (e.isControlDown())
         {
            if (e.getCode() == KeyCode.S && !saveChangesButton.isDisabled())
            {
               saveChangesButton.fire();
            }
            else if (e.getCode() == KeyCode.Z && !revertChangesButton.isDisabled())
            {
               revertChangesButton.fire();
            }
         }
      });

      window = new Stage(StageStyle.UTILITY);
      window.addEventHandler(KeyEvent.KEY_PRESSED, e ->
      {
         if (e.getCode() == KeyCode.ESCAPE)
            window.close();
      });
      toolkit.getMainWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
      {
         if (!e.isConsumed())
            window.close();
      });
      window.setTitle("YoGraphic properties");
      window.setScene(new Scene(mainAnchorPane));
      window.initOwner(toolkit.getMainWindow());
   }

   public void showWindow()
   {
      window.setOpacity(0.0);
      window.toFront();
      window.show();
      Timeline timeline = new Timeline();
      KeyFrame key = new KeyFrame(Duration.seconds(0.125), new KeyValue(window.opacityProperty(), 1.0));
      timeline.getKeyFrames().add(key);
      timeline.play();
   }

   public void closeAndDispose()
   {
      window.close();
   }

   private final SetChangeListener<YoGraphicFXItem> treeViewAutoRefreshListener = new SetChangeListener<YoGraphicFXItem>()
   {
      @Override
      public void onChanged(Change<? extends YoGraphicFXItem> change)
      {
         if (change.wasAdded())
         {
            change.getElementAdded().getItemChildren().addListener(treeViewAutoRefreshListener);
         }
         else if (change.wasRemoved())
         {
            change.getElementRemoved().getItemChildren().removeListener(treeViewAutoRefreshListener);
         }

         JavaFXMissingTools.runLater(getClass(), () ->
         {
            ignoreTreeSelectionUpdate = true;
            refreshTreeView();
            ignoreTreeSelectionUpdate = false;
         });
      }
   };

   private void initializeTreeViewAutoRefreshListener(YoGraphicFXItem item)
   {
      item.getItemChildren().addListener(treeViewAutoRefreshListener);

      for (YoGraphicFXItem child : item.getItemChildren())
         initializeTreeViewAutoRefreshListener(child);
   }

   private void refreshTreeView()
   {
      unloadEditor();
      rootGroup.updateVisiblePropertyRecursively();
      CheckBoxTreeItem<YoGraphicFXItem> oldRootItem = rootItem;
      rootItem = new CheckBoxTreeItem<>(rootGroup);
      rootItem.setExpanded(true);
      bindVisibilityProperty(rootItem);
      buildTreeRecursively(rootItem);
      yoGraphicTreeView.setRoot(rootItem);
      copyExpandedPropertyRecursively(oldRootItem, rootItem);
   }

   private void expandTreeView(TreeItem<?> item)
   {
      if (item != null && !item.isLeaf())
      {
         item.setExpanded(true);

         for (TreeItem<?> child : item.getChildren())
            expandTreeView(child);
      }
   }

   private void copyExpandedPropertyRecursively(TreeItem<?> reference, TreeItem<?> item)
   {
      if (item != null && !item.isLeaf())
      {
         for (TreeItem<?> child : item.getChildren())
         {
            if (reference == null)
            {
               expandTreeView(child);
            }
            else
            {
               TreeItem<?> referenceChild = reference.getChildren().stream().filter(refChild -> refChild.getValue() == child.getValue()).findFirst()
                                                     .orElse(null);
               if (referenceChild == null)
               {
                  expandTreeView(child);
               }
               else
               {
                  child.setExpanded(referenceChild.isExpanded());
                  copyExpandedPropertyRecursively(referenceChild, child);
               }
            }
         }
      }
   }

   private void bindVisibilityProperty(CheckBoxTreeItem<YoGraphicFXItem> treeItem)
   {
      treeItem.setSelected(treeItem.getValue().isVisible());
      treeItem.setIndeterminate(areChildrenOnlyPartiallyVisible(treeItem.getValue()));

      MutableBoolean isBindingUpdate = new MutableBoolean(false);

      treeItem.selectedProperty().addListener((observable, oldValue, newValue) ->
      {
         if (isBindingUpdate.booleanValue() || treeItem.isIndeterminate() || treeItem.getValue() == null)
            return;
         isBindingUpdate.setTrue();
         treeItem.getValue().setVisible(newValue);
         isBindingUpdate.setFalse();
      });

      treeItem.valueProperty().addListener(new ChangeListener<YoGraphicFXItem>()
      {
         private YoGraphicFXItem currentItem = null;

         private final ChangeListener<? super Boolean> fxItemVisibleListener = (o, oldValue, newValue) ->
         {
            if (isBindingUpdate.booleanValue())
               return;
            isBindingUpdate.setTrue();
            treeItem.setSelected(newValue);
            treeItem.setIndeterminate(areChildrenOnlyPartiallyVisible(currentItem));
            isBindingUpdate.setFalse();
         };

         @Override
         public void changed(ObservableValue<? extends YoGraphicFXItem> observable, YoGraphicFXItem oldFXItem, YoGraphicFXItem newFXItem)
         {
            oldFXItem.visibleProperty().removeListener(fxItemVisibleListener);
            currentItem = newFXItem;
            newFXItem.visibleProperty().addListener(fxItemVisibleListener);
         }
      });
   }

   private static boolean areChildrenOnlyPartiallyVisible(YoGraphicFXItem item)
   {
      boolean areAllChildrenVisible = item.getItemChildren().stream().allMatch(YoGraphicFXItem::isVisible);
      if (areAllChildrenVisible)
         return false;
      boolean areNoChildrenVisible = item.getItemChildren().stream().noneMatch(YoGraphicFXItem::isVisible);
      if (areNoChildrenVisible)
         return false;
      return true;
   }

   private void selectItem(TreeItem<YoGraphicFXItem> treeItem, YoGraphicFXItem itemToSelect)
   {
      if (treeItem == null)
         return;
      if (treeItem.getValue() == itemToSelect)
      {
         yoGraphicTreeView.getSelectionModel().select(treeItem);
         return;
      }

      for (TreeItem<YoGraphicFXItem> child : treeItem.getChildren())
         selectItem(child, itemToSelect);
   }

   private boolean ignoreTreeSelectionUpdate = false;

   private void processTreeSelectionUpdate(TreeItem<YoGraphicFXItem> oldSelectedValue, TreeItem<YoGraphicFXItem> newSelectedValue)
   {
      if (ignoreTreeSelectionUpdate)
         return;

      if (shouldCancelAction(oldSelectedValue))
         return;

      unloadEditor();

      if (newSelectedValue == null)
         return;

      YoGraphicFXItem item = newSelectedValue.getValue();

      Class<? extends YoGraphicFXItem> itemType = item.getClass();

      if (item instanceof YoGroupFX)
         return;

      YoGraphicFXCreatorController<YoGraphicFX> controller = cachedEditors.get(item);

      if (controller == null)
      {
         try
         {
            FXMLLoader loader = SessionVisualizerIOTools.getYoGraphicFXEditorFXMLLoader(itemType);
            loader.load();
            controller = loader.getController();
            controller.initialize(toolkit, (YoGraphicFX) item);
            cachedEditors.put(item, controller);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      if (controller != null)
      {
         Pane editorPane = controller.getMainPane();
         activeEditor.set(controller);
         yoGraphicEditorPane.getChildren().add(editorPane);
         AnchorPane.setLeftAnchor(editorPane, 0.0);
         AnchorPane.setRightAnchor(editorPane, 0.0);
         AnchorPane.setTopAnchor(editorPane, 0.0);
         AnchorPane.setBottomAnchor(editorPane, 0.0);
         yoGraphicTypeLabel.setText(itemType.getSimpleName());
      }
   }

   private void unloadEditor()
   {
      yoGraphicEditorPane.getChildren().clear();
      activeEditor.set(null);
   }

   @FXML
   public void addItem()
   {
      if (shouldCancelAction(null))
         return;

      TreeItem<YoGraphicFXItem> selectedItem = yoGraphicTreeView.getSelectionModel().getSelectedItem();
      YoGroupFX group;

      if (selectedItem == null)
      {
         group = rootGroup;
      }
      else
      {
         YoGraphicFXItem yoGraphicFXItem = selectedItem.getValue();

         if (yoGraphicFXItem instanceof YoGraphicFX)
            group = ((YoGraphicFX) yoGraphicFXItem).parentGroupProperty().get();
         else if (yoGraphicFXItem instanceof YoGroupFX)
            group = ((YoGroupFX) yoGraphicFXItem);
         else
            throw new RuntimeException("Unexpected item type: " + yoGraphicFXItem.getClass().getSimpleName());
      }

      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.YO_GRAPHIC_ITEM_CREATOR_URL);
         loader.load();
         YoGraphicItemCreatorDialogController controller = loader.getController();
         controller.initialize(toolkit, group);
         controller.showAndWait();

         Optional<String> itemNameResult = controller.getItemNameResult();
         Optional<Class<? extends YoGraphicFXItem>> itemTypeResult = controller.getItemTypeResult();

         if (itemNameResult.isPresent() && itemTypeResult.isPresent())
         {
            YoGraphicFXItem newItem = YoGraphicFXControllerTools.createYoGraphicFXItemAndRegister(group, itemNameResult.get(), itemTypeResult.get());

            JavaFXMissingTools.runLater(getClass(), () ->
            {
               selectItem(rootItem, newItem);
               yoGraphicTreeView.requestFocus();
            });
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @FXML
   public void removeItem()
   {
      if (shouldCancelAction(null))
         return;

      TreeItem<YoGraphicFXItem> selectedItem = yoGraphicTreeView.getSelectionModel().getSelectedItem();

      if (selectedItem == null || selectedItem.getValue().parentGroupProperty() == null)
         return;

      int selectedIndex = yoGraphicTreeView.getSelectionModel().getSelectedIndex();
      ObservableList<TreeItem<YoGraphicFXItem>> itemsToRemove = yoGraphicTreeView.getSelectionModel().getSelectedItems();

      int nextSelectedIndexGuess = selectedIndex - 1;
      TreeItem<YoGraphicFXItem> nextSelectedItemGuess = yoGraphicTreeView.getTreeItem(nextSelectedIndexGuess);

      if (nextSelectedItemGuess == null || itemsToRemove.contains(nextSelectedItemGuess))
      {
         nextSelectedIndexGuess = selectedIndex + 1;
         nextSelectedItemGuess = yoGraphicTreeView.getTreeItem(nextSelectedIndexGuess);

         if (nextSelectedItemGuess == null || itemsToRemove.contains(nextSelectedItemGuess))
         {
            nextSelectedIndexGuess = selectedIndex - 1;
            nextSelectedItemGuess = yoGraphicTreeView.getTreeItem(nextSelectedIndexGuess);

            do
            {
               nextSelectedIndexGuess--;
               if (nextSelectedIndexGuess < 0)
               {
                  nextSelectedItemGuess = null;
                  nextSelectedIndexGuess = 0;
                  break;
               }
               nextSelectedItemGuess = yoGraphicTreeView.getTreeItem(nextSelectedIndexGuess);
            }
            while (nextSelectedItemGuess == null || itemsToRemove.contains(nextSelectedItemGuess));
         }
      }

      YoGraphicFXItem nextSelectedItem = nextSelectedItemGuess == null ? null : nextSelectedItemGuess.getValue();

      for (TreeItem<YoGraphicFXItem> itemToRemove : itemsToRemove)
      {
         YoGraphicFXItem yoGraphicFXItem = itemToRemove.getValue();

         if (!yoGraphicFXItem.getItemChildren().isEmpty())
         {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setContentText("Do you really want to delete this group and all its children?");
            alert.initOwner(window);
            ButtonType answer = alert.showAndWait().get();
            if (answer.getButtonData().isCancelButton())
               return;
         }

         yoGraphicFXItem.clear();

         if (yoGraphicFXItem != sessionRootGroup)
            yoGraphicFXItem.detachFromParent();
      }

      cachedEditors.remove(selectedItem.getValue());

      JavaFXMissingTools.runLater(getClass(), () ->
      {
         ignoreTreeSelectionUpdate = true;
         yoGraphicTreeView.getSelectionModel().clearSelection();
         ignoreTreeSelectionUpdate = false;
         if (nextSelectedItem == null)
            yoGraphicTreeView.getSelectionModel().select(0);
         else
            selectItem(rootItem, nextSelectedItem);
         yoGraphicTreeView.requestFocus();
      });
   }

   public void duplicateItem()
   {
      if (shouldCancelAction(null))
         return;

      TreeItem<YoGraphicFXItem> selectedItem = yoGraphicTreeView.getSelectionModel().getSelectedItem();

      if (selectedItem == null)
         return;

      YoGraphicFXItem newItem = YoGraphicFXControllerTools.duplicateYoGraphicFXItemAndRegister(selectedItem.getValue());

      JavaFXMissingTools.runLater(getClass(), () ->
      {
         selectItem(rootItem, newItem);
         yoGraphicTreeView.requestFocus();
      });
   }

   @FXML
   public void saveChanges()
   {
      if (activeEditor.get() != null)
         activeEditor.get().saveChanges();
   }

   @FXML
   public void cancelChanges()
   {
      if (activeEditor.get() != null)
         activeEditor.get().resetFields();
   }

   @FXML
   public void exportYoGraphicFXItems()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationSaveFileDialog(window);
      if (result != null)
         messager.submitMessage(topics.getYoGraphicSaveRequest(), result);
   }

   @FXML
   public void importYoGraphicFXItems()
   {
      File result = SessionVisualizerIOTools.yoGraphicConfigurationOpenFileDialog(window);
      if (result != null)
         messager.submitMessage(topics.getYoGraphicLoadRequest(), result);
   }

   public Stage getWindow()
   {
      return window;
   }

   private boolean shouldCancelAction(TreeItem<YoGraphicFXItem> itemToSelectOnCancel)
   {
      if (activeEditor.get() != null && activeEditor.get().hasChangesPending())
      {
         Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to discard the changes?", ButtonType.YES, ButtonType.NO);
         Optional<ButtonType> result = alert.showAndWait();
         ButtonType answer = result.get();

         if (answer == ButtonType.YES)
         {
            cancelChanges();
            return false;
         }
         else if (answer == ButtonType.NO)
         {
            if (itemToSelectOnCancel != null)
            {
               JavaFXMissingTools.runLater(getClass(), () ->
               {
                  ignoreTreeSelectionUpdate = true;
                  yoGraphicTreeView.getSelectionModel().clearSelection();
                  yoGraphicTreeView.getSelectionModel().select(itemToSelectOnCancel);
                  ignoreTreeSelectionUpdate = false;
               });
            }
            return true;
         }
      }

      return false;
   }

   private void buildTreeRecursively(TreeItem<YoGraphicFXItem> parent)
   {
      for (YoGraphicFXItem child : parent.getValue().getItemChildren())
      {
         CheckBoxTreeItem<YoGraphicFXItem> childItem = new CheckBoxTreeItem<>(child);
         bindVisibilityProperty(childItem);
         parent.getChildren().add(childItem);
         buildTreeRecursively(childItem);
      }
   }

   public void handleDragDetected(MouseEvent event)
   {
      List<YoGraphicFXItem> items = yoGraphicTreeView.getSelectionModel().getSelectedItems().stream().map(TreeItem<YoGraphicFXItem>::getValue)
                                                     .collect(Collectors.toList());

      Dragboard dragBoard = yoGraphicTreeView.startDragAndDrop(TransferMode.ANY);
      dragBoard.setContent(DragAndDropTools.toClipboardContent(items));
      event.consume();
   }
}
