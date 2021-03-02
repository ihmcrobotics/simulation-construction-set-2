package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.entry;

import static us.ihmc.scs2.sessionVisualizer.jfx.tools.ListViewTools.removeMenuItemFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoEntry.YoEntryDefinition;
import us.ihmc.scs2.definition.yoEntry.YoEntryListDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.search.YoCompositeListCell;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ContextMenuTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeCollection;

public class YoEntryListViewController
{
   @FXML
   private ListView<YoComposite> yoEntryListView;

   private final BooleanProperty showUniqueNamesProperty = new SimpleBooleanProperty(this, "showUniqueNames", false);
   private final StringProperty nameProperty = new SimpleStringProperty(this, "name", null);
   private YoCompositeSearchManager yoCompositeSearchManager;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      yoEntryListView.setCellFactory(param -> new YoCompositeListCell(toolkit.getYoManager(), showUniqueNamesProperty, param));
      yoEntryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      ContextMenuTools.setupContextMenu(yoEntryListView, removeMenuItemFactory(true));

      yoEntryListView.setOnDragDetected(this::handleDragDetected);
      yoEntryListView.setOnDragEntered(event -> handleDragEntered(event));
      yoEntryListView.setOnDragExited(event -> handleDragExited(event));
      yoEntryListView.setOnDragOver(event -> handleDragOver(event));
      yoEntryListView.setOnDragDropped(event -> handleDragDropped(event));
   }

   public void setInput(YoEntryListDefinition input)
   {
      if (input.getName() != null)
         nameProperty.set(input.getName());

      yoEntryListView.getItems().clear();

      if (input.getYoEntries() == null)
         return;

      for (YoEntryDefinition entry : input.getYoEntries())
      {
         String type = entry.getCompositeType();
         String fullname = entry.getCompositeFullname();

         YoCompositeCollection collection = yoCompositeSearchManager.getCollectionFromType(type);
         if (collection != null)
         {
            YoComposite yoComposite = collection.getYoCompositeFromFullname(fullname);
            if (yoComposite != null)
               yoEntryListView.getItems().add(yoComposite);
            else
               LogTools.warn("Could not find composite: " + fullname);
         }
         else
         {
            LogTools.warn("Could not find composite type: " + type);
         }
      }
   }

   public YoEntryListDefinition toYoEntryListDefinition()
   {
      YoEntryListDefinition definition = new YoEntryListDefinition();
      definition.setName(nameProperty.get());
      definition.setYoEntries(new ArrayList<>());

      for (YoComposite entry : yoEntryListView.getItems())
      {
         YoEntryDefinition yoEntryDefinition = new YoEntryDefinition();
         yoEntryDefinition.setCompositeType(entry.getPattern().getType());
         yoEntryDefinition.setCompositeFullname(entry.getFullname());
         definition.getYoEntries().add(yoEntryDefinition);
      }
      return definition;
   }

   public StringProperty nameProperty()
   {
      return nameProperty;
   }

   public void clear()
   {
      yoEntryListView.getItems().clear();
   }

   public boolean isEmpty()
   {
      return yoEntryListView.getItems().isEmpty();
   }

   public void handleDragDetected(MouseEvent event)
   {
      if (!event.isPrimaryButtonDown())
         return;

      YoComposite yoComposite = yoEntryListView.getSelectionModel().getSelectedItem();

      Dragboard dragBoard = yoEntryListView.startDragAndDrop(TransferMode.ANY);
      ClipboardContent clipboardContent = new ClipboardContent();
      clipboardContent.put(DragAndDropTools.YO_COMPOSITE_REFERENCE, Arrays.asList(yoComposite.getPattern().getType(), yoComposite.getFullname()));
      dragBoard.setContent(clipboardContent);
      event.consume();
   }

   private void handleDragEntered(DragEvent event)
   {
      if (!event.isAccepted() && acceptDragEventForDrop(event))
         setSelectionHighlight(true);
      event.consume();
   }

   public void handleDragExited(DragEvent event)
   {
      if (acceptDragEventForDrop(event))
         setSelectionHighlight(false);
      event.consume();
   }

   public void handleDragOver(DragEvent event)
   {
      if (!event.isAccepted() && acceptDragEventForDrop(event))
         event.acceptTransferModes(TransferMode.ANY);
      event.consume();
   }

   public void handleDragDropped(DragEvent event)
   {
      Dragboard db = event.getDragboard();
      boolean success = false;

      List<YoComposite> yoComposites = DragAndDropTools.retrieveYoCompositesFromDragBoard(db, yoCompositeSearchManager);

      if (yoComposites != null)
      {
         yoEntryListView.getItems().addAll(yoComposites);
         success = true;
      }

      event.setDropCompleted(success);
      event.consume();
   }

   private boolean acceptDragEventForDrop(DragEvent event)
   {
      if (event.getGestureSource() == yoEntryListView)
         return false;

      Dragboard dragboard = event.getDragboard();
      return DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoCompositeSearchManager) != null;
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         yoEntryListView.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         yoEntryListView.setStyle("-fx-border-color: null;");
   }
}
