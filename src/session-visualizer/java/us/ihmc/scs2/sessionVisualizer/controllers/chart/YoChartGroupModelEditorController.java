package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXTextField;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import us.ihmc.scs2.definition.yoChart.YoChartGroupModelDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.controllers.ControllerListCell;
import us.ihmc.scs2.sessionVisualizer.controllers.SCSDefaultUIController;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public class YoChartGroupModelEditorController implements SCSDefaultUIController
{
   @FXML
   private VBox mainPane;
   @FXML
   private Label configurationNameLabel;
   @FXML
   private JFXTextField configurationNameTextField;
   @FXML
   private Label configurationModelLabel;
   @FXML
   private ListView<YoChartIdentifierEditorController> listView;

   private final ObjectProperty<YoChartGroupModelDefinition> chartGroupModelProperty = new SimpleObjectProperty<>(this, "chartGroupModel", null);

   private SessionVisualizerToolkit toolkit;
   private Window owner;

   @Override
   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      this.toolkit = toolkit;
      this.owner = owner;

      listView.setCellFactory(param -> new ControllerListCell<>());

      listView.getItems().addListener((ListChangeListener<YoChartIdentifierEditorController>) change ->
      {
         ObservableList<? extends YoChartIdentifierEditorController> newList = change.getList();
         { // Initialize with the new list values. 
            YoChartGroupModelDefinition newModel = new YoChartGroupModelDefinition(chartGroupModelProperty.get());
            newModel.setChartIdentifiers(newList.stream().map(controller -> controller.chartIdentifierProperty().get()).collect(Collectors.toList()));
            chartGroupModelProperty.set(newModel);
         }

         for (int i = 0; i < newList.size(); i++)
         { // Setup listeners for each individual controller.
            int indexFinal = i;
            newList.get(i).chartIdentifierProperty().addListener((o, oldValue, newValue) ->
            {
               YoChartGroupModelDefinition updatedModel = new YoChartGroupModelDefinition(chartGroupModelProperty.get());
               updatedModel.getChartIdentifiers().set(indexFinal, newValue);
               chartGroupModelProperty.set(updatedModel);
            });
         }
      });

      configurationNameTextField.textProperty().addListener((o, oldValue, newValue) ->
      {
         YoChartGroupModelDefinition newModel = new YoChartGroupModelDefinition(chartGroupModelProperty.get());
         newModel.setName(newValue);
         chartGroupModelProperty.set(newModel);
      });
      setPrefHeight();
   }

   public void setInput(YoChartGroupModelDefinition input, List<? extends String> chartIdNames)
   {
      configurationNameTextField.setText(input.getName());

      ObservableList<YoChartIdentifierEditorController> listItems = listView.getItems();
      while (listItems.size() < input.getChartIdentifiers().size())
         listItems.add(newYoChartIdentifierEditor());
      while (listItems.size() > input.getChartIdentifiers().size())
         listItems.remove(listItems.size() - 1);
      for (int i = 0; i < listItems.size(); i++)
         listItems.get(i).setInput(input.getChartIdentifiers().get(i));

      if (chartIdNames != null)
         setChartIdentifierNames(chartIdNames);
   }

   public void setChartIdentifierNames(List<? extends String> chartIdNames)
   {
      ObservableList<YoChartIdentifierEditorController> listItems = listView.getItems();
      if (chartIdNames.size() != listItems.size())
         throw new IllegalArgumentException("The chartIdNames has to have the same size as the number of chart identifiers.");

      for (int i = 0; i < listItems.size(); i++)
      {
         listItems.get(i).getChartIdLabel().setText(chartIdNames.get(i));
      }
   }

   private AnimationTimer prefHeightAdjustmentAnimation;

   private void setPrefHeight()
   {
      if (prefHeightAdjustmentAnimation == null)
      {
         prefHeightAdjustmentAnimation = new AnimationTimer()
         {
            @Override
            public void handle(long now)
            {
               if (listView.getItems().isEmpty())
               {
                  listView.setMinHeight(0.0);
                  listView.setMaxHeight(0.0);
                  listView.setPrefHeight(0.0);
               }
               else
               {
                  double minHeight = listView.getItems().get(0).getMainPane().getHeight() + 10.0;
                  int size = listView.getItems().size();
                  listView.setMinHeight(minHeight);
                  listView.setPrefHeight(size * minHeight);
                  listView.setMaxHeight(2.0 * size * minHeight);
               }
            }
         };
      }

      prefHeightAdjustmentAnimation.start();
   }

   private YoChartIdentifierEditorController newYoChartIdentifierEditor()
   {
      FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.CHART_IDENTIFIER_EDITOR_PANE_URL);
      try
      {
         loader.load();
         YoChartIdentifierEditorController editor = loader.getController();
         editor.initialize(toolkit, owner);
         return editor;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void startEditingChartGroupModelName()
   {
      configurationNameTextField.requestFocus();
   }

   public StringProperty configurationNameProperty()
   {
      return configurationNameTextField.textProperty();
   }

   public ReadOnlyObjectProperty<YoChartGroupModelDefinition> chartGroupModelProperty()
   {
      return chartGroupModelProperty;
   }

   @Override
   public Pane getMainPane()
   {
      return mainPane;
   }
}
