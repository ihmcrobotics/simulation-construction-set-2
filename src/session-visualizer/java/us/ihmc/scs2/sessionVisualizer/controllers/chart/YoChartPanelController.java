package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import com.sun.javafx.scene.control.skin.LabeledText;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.NumericAxis;
import de.gsi.chart.plugins.XValueIndicator;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import us.ihmc.commons.MathTools;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DReadOnly;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.messager.TopicListener;
import us.ihmc.scs2.definition.yoChart.ChartDoubleBoundsDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartConfigurationDefinition;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.charts.*;
import us.ihmc.scs2.sessionVisualizer.charts.YoVariableChartData.ChartDataUpdate;
import us.ihmc.scs2.sessionVisualizer.managers.ChartDataManager;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.CompositePropertyTools.YoVariableDatabase;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoCompositeTools;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoChartPanelController extends AnimationTimer
{
   private static final String INPOINT_MARKER_STYLECLASS = "chart-inpoint-marker";
   private static final String OUTPOINT_MARKER_STYLECLASS = "chart-outpoint-marker";
   private static final String CURRENT_INDEX_MARKER_STYLECLASS = "chart-current-index-marker";
   private static final String ORIGIN_MARKER_STYLECLASS = "chart-origin-marker";
   private static final String KEYFRAME_MARKER_STYLECLASS = "chart-keyframe-marker";

   @FXML
   private AnchorPane chartMainPane;
   @FXML
   private JFXButton closeButton;

   private final NumericAxis xAxis = new NumericAxis();
   private final NumericAxis yAxis = new NumericAxis();
   private XYChart lineChart;
   private final YoChartLegend yoLegend = new YoChartLegend();

   private XValueIndicator inPointIndicator, outPointIndicator, bufferIndexIndicator;

   private final List<XValueIndicator> keyFrameIndicators = new ArrayList<>();

   private YoCompositeSearchManager yoCompositeSearchManager;

   private ChartDataManager chartDataManager;
   private final ObservableList<YoNumberSeries> yoNumberSeriesList = FXCollections.observableArrayList();
   private final ObservableMap<YoVariable<?>, YoVariableChartPackage> charts = FXCollections.observableMap(new LinkedHashMap<>());
   private AtomicReference<YoBufferPropertiesReadOnly> bufferPropertiesForMarkers;
   private AtomicReference<YoBufferPropertiesReadOnly> bufferPropertiesForScrolling;
   private final TopicListener<int[]> keyFrameMarkerListener = newKeyFrames -> updateKeyFrameMarkers(newKeyFrames);

   private final SimpleObjectProperty<ContextMenu> contextMenuProperty = new SimpleObjectProperty<ContextMenu>(this, "graphContextMenu", null);

   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;
   private YoManager yoManager;
   private SessionVisualizerToolkit toolkit;
   private Window parentWindow;

   public void initialize(SessionVisualizerToolkit toolkit, Window parentWindow)
   {
      this.toolkit = toolkit;
      this.parentWindow = parentWindow;
      this.messager = toolkit.getMessager();
      this.chartDataManager = toolkit.getChartDataManager();
      this.yoManager = toolkit.getYoManager();
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();
      topics = toolkit.getTopics();

      bufferPropertiesForMarkers = messager.createInput(topics.getYoBufferCurrentProperties());
      bufferPropertiesForScrolling = messager.createInput(topics.getYoBufferCurrentProperties());

      lineChart = new XYChart(xAxis, yAxis);
      // Removing the side-tools, we won't use them.
      lineChart.setTop(null);
      lineChart.setBottom(null);
      lineChart.setLeft(null);
      lineChart.setRight(null);
      lineChart.setLegend(yoLegend);
      // TODO Workaround to get the legend to show up. Remove when fixed.
      lineChart.setLegendVisible(false);
      lineChart.setLegendVisible(true);
      lineChart.setAnimated(false);
      lineChart.setHorizontalGridLinesVisible(false);
      lineChart.setVerticalGridLinesVisible(false);
      ErrorDataSetRenderer errorDataSetRenderer = new ErrorDataSetRenderer();
      errorDataSetRenderer.drawMarkerProperty().set(false);
      errorDataSetRenderer.errorStyleProperty().set(ErrorStyle.NONE);
      lineChart.getRenderers().setAll(errorDataSetRenderer);
      // We won't use the title, removing it to save some space.
      lineChart.getTitleLegendPane(Side.TOP).getChildren().clear();

      xAxis.set(0.0, 1000.0);
      xAxis.setMinorTickLength(0);
      xAxis.setMinorTickVisible(false);
      xAxis.setTickLabelsVisible(false);
      xAxis.setTickMarkVisible(false);
      xAxis.setAnimated(false);
      xAxis.setAutoRanging(false);

      yAxis.set(0.0, 1.0);
      yAxis.setMinorTickVisible(false);
      yAxis.setTickLabelsVisible(false);
      yAxis.setTickMarkVisible(false);
      yAxis.setAnimated(false);
      yAxis.setAutoRanging(true);
      yAxis.setForceZeroInRange(false);

      // TODO Make custom indicator without label or triangle?
      inPointIndicator = new XValueIndicator(xAxis, 0.0)
      {
         @Override
         public void updateStyleClass()
         {
            //            super.updateStyleClass();
            line.getStyleClass().add(INPOINT_MARKER_STYLECLASS);
         }
      };
      outPointIndicator = new XValueIndicator(xAxis, 0.0)
      {
         @Override
         public void updateStyleClass()
         {
            //            super.updateStyleClass();
            line.getStyleClass().add(OUTPOINT_MARKER_STYLECLASS);
         }
      };
      bufferIndexIndicator = new XValueIndicator(xAxis, 0.0)
      {
         @Override
         public void updateStyleClass()
         {
            //            super.updateStyleClass();
            line.getStyleClass().add(CURRENT_INDEX_MARKER_STYLECLASS);
         }
      };
      lineChart.getPlugins().addAll(inPointIndicator, outPointIndicator, bufferIndexIndicator);
      //
      //      Data<Number, Number> origin = new Data<>(0.0, 0.0);
      //      ChangeListener<? super ChartStyle> originMarkerUpdater = (o, oldValue, newValue) ->
      //      {
      //         if (newValue == ChartStyle.RAW)
      //         {
      //            ChartMarker originMarker = dynamicLineChart.addMarker(origin);
      //            originMarker.getStyleClass().add(ORIGIN_MARKER_STYLECLASS);
      //         }
      //         else
      //         {
      //            dynamicLineChart.removeMarker(origin);
      //         }
      //      };
      //      dynamicLineChart.chartStyleProperty().addListener(originMarkerUpdater);
      //      originMarkerUpdater.changed(null, null, dynamicLineChart.chartStyleProperty().get());

      lineChart.setOnDragDetected(this::handleDragDetected);
      lineChart.setOnDragOver(this::handleDragOver);
      lineChart.setOnDragDropped(this::handleDragDropped);
      lineChart.setOnDragEntered(this::handleDragEntered);
      lineChart.setOnDragExited(this::handleDragExited);
      lineChart.setOnMousePressed(this::handleMousePressed);
      lineChart.setOnMouseDragged(this::handleMouseDrag);
      lineChart.setOnMouseReleased(this::handleMouseReleased);
      lineChart.setOnScroll(this::handleScroll);
      lineChart.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMouseMiddleClick);

      contextMenuProperty.addListener((ChangeListener<ContextMenu>) (observable, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.hide();
      });
      charts.addListener((MapChangeListener<YoVariable<?>, YoVariableChartPackage>) change ->
      {
         if (change.wasAdded())
            yoNumberSeriesList.add(change.getValueAdded().getSeries());
         else if (change.wasRemoved())
            yoNumberSeriesList.remove(change.getValueRemoved().getSeries());
      });

      chartMainPane.getChildren().add(lineChart);
      AnchorPane.setTopAnchor(lineChart, 0.0);
      AnchorPane.setBottomAnchor(lineChart, 0.0);
      AnchorPane.setLeftAnchor(lineChart, 0.0);
      AnchorPane.setRightAnchor(lineChart, 0.0);

      messager.registerJavaFXSyncedTopicListener(topics.getCurrentKeyFrames(), keyFrameMarkerListener);
      messager.submitMessage(topics.getRequestCurrentKeyFrames(), new Object());
   }

   public void setChartConfiguration(YoChartConfigurationDefinition definition)
   {
      clear();
      if (definition.getYoVariables() != null)
      {
         definition.getYoVariables().forEach(this::addYoVariableToPlot);
      }
      else
      {
         // TODO
         //         dynamicLineChart.setChartStyle(ChartStyle.RAW);
         return;
      }

      if (definition.getChartStyle() != null)
      {
         try
         {
            // TODO
            //            dynamicLineChart.setChartStyle(ChartStyle.valueOf(definition.getChartStyle()));
         }
         catch (IllegalArgumentException e)
         {
            // TOOD
            //            dynamicLineChart.setChartStyle(ChartStyle.RAW);
         }
      }
      else
      {
         // TODO
         //         dynamicLineChart.setChartStyle(ChartStyle.RAW);
      }

      for (YoVariableChartPackage pack : charts.values())
      {
         int definitionIndex = definition.getYoVariables().indexOf(pack.getYoVariable().getFullNameWithNameSpace());
         if (definitionIndex == -1)
            continue;

         if (definition.getYBounds() != null && definition.getYBounds().size() > definitionIndex)
         {
            ChartDoubleBoundsDefinition yBounds = definition.getYBounds().get(definitionIndex);
            pack.series.setCustomYBounds(YoChartTools.toChartDoubleBounds(yBounds));
         }

         if (definition.getNegates() != null && definition.getNegates().size() > definitionIndex)
         {
            Boolean negate = definition.getNegates().get(definitionIndex);
            pack.series.setNegated(negate);
         }
      }
   }

   private final ObjectProperty<YoChartOptionController> activeChartOptionControllerProperty = new SimpleObjectProperty<>(this,
                                                                                                                          "activeChartOptionController",
                                                                                                                          null);

   @FXML
   public void openChartOptionDialog()
   {
      if (activeChartOptionControllerProperty.get() != null)
      {
         // TODO
         //         activeChartOptionControllerProperty.get().setInput(yoNumberSeriesList, dynamicLineChart.chartStyleProperty());
         activeChartOptionControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.CHART_OPTION_DIALOG_URL);
         loader.load();
         YoChartOptionController controller = loader.getController();
         controller.initialize(toolkit, parentWindow);
         // TODO
         //         controller.setInput(yoNumberSeriesList, dynamicLineChart.chartStyleProperty());
         activeChartOptionControllerProperty.set(controller);
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void clear()
   {
      new ArrayList<>(charts.keySet()).forEach(this::removeYoVariableFromPlot);
   }

   public void addYoVariableToPlot(String yoVariableFullName)
   {
      YoVariableDatabase rootRegistryDatabase = yoManager.getRootRegistryDatabase();
      YoVariable<?> yoVariable = rootRegistryDatabase.searchExact(yoVariableFullName);
      if (yoVariable == null)
      {
         LogTools.warn("Incompatible variable name, searching similar variables to " + yoVariableFullName);
         yoVariable = rootRegistryDatabase.searchSimilar(yoVariableFullName, 0.90);
      }
      if (yoVariable == null)
      {
         LogTools.warn("Could not find YoVariable: " + yoVariableFullName);
         return;
      }
      addYoVariableToPlot(yoVariable);
   }

   public void addYoVariableToPlot(YoVariable<?> yoVariable)
   {
      if (charts.containsKey(yoVariable))
         return;
      charts.put(yoVariable, new YoVariableChartPackage(yoVariable));
   }

   public void addYoVariablesToPlot(Collection<? extends YoVariable<?>> yoVariables)
   {
      yoVariables.forEach(this::addYoVariableToPlot);
   }

   public void addYoCompositeToPlot(YoComposite yoComposite)
   {
      addYoVariablesToPlot(yoComposite.getYoComponents());
   }

   public void removeYoVariableFromPlot(String yoVariableFullName)
   {
      removeYoVariableFromPlot(yoManager.getRootRegistry().getVariable(yoVariableFullName));
   }

   public void removeYoVariableFromPlot(YoVariable<?> yoVariable)
   {
      YoVariableChartPackage chart = charts.remove(yoVariable);
      if (chart != null)
         chart.close();
   }

   public void close()
   {
      if (activeChartOptionControllerProperty.get() != null)
         activeChartOptionControllerProperty.get().close();

      ArrayList<YoVariableChartPackage> chartsCopy = new ArrayList<>(charts.values());
      charts.clear();
      chartsCopy.forEach(YoVariableChartPackage::close);

      messager.removeInput(topics.getYoBufferCurrentProperties(), bufferPropertiesForMarkers);
      messager.removeInput(topics.getYoBufferCurrentProperties(), bufferPropertiesForScrolling);
      messager.removeJavaFXSyncedTopicListener(topics.getCurrentKeyFrames(), keyFrameMarkerListener);
   }

   public boolean isEmpty()
   {
      return charts.isEmpty();
   }

   private void updateKeyFrameMarkers(int[] newKeyFrames)
   {
      lineChart.getPlugins().removeAll(keyFrameIndicators);
      keyFrameIndicators.clear();

      for (int keyFrame : newKeyFrames)
      {
         keyFrameIndicators.add(new XValueIndicator(xAxis, keyFrame)
         {
            @Override
            public void updateStyleClass()
            {
               line.getStyleClass().add(KEYFRAME_MARKER_STYLECLASS);
            }
         });
      }
      lineChart.getPlugins().addAll(keyFrameIndicators);
   }

   @Override
   public void handle(long now)
   {
      ChartIntegerBounds chartsBounds = chartDataManager.chartBoundsProperty().getValue();
      YoBufferPropertiesReadOnly bufferProperties = bufferPropertiesForMarkers.getAndSet(null);

      if (bufferProperties != null)
      {
         if (bufferProperties.getInPoint() != inPointIndicator.getValue())
            inPointIndicator.setValue(bufferProperties.getInPoint());
         if (bufferProperties.getOutPoint() != outPointIndicator.getValue())
            outPointIndicator.setValue(bufferProperties.getOutPoint());
         if (bufferProperties.getCurrentIndex() != bufferIndexIndicator.getValue())
            bufferIndexIndicator.setValue(bufferProperties.getCurrentIndex());
         if (chartsBounds == null)
         {
            double scale = 0.001;
            xAxis.set(-scale * bufferProperties.getSize(), (1.0 + scale) * bufferProperties.getSize());
         }

         bufferProperties = null;
      }

      if (chartsBounds != null)
      {
         double scale = 0.001;
         xAxis.set(chartsBounds.getLower() - scale * chartsBounds.length(), chartsBounds.getUpper() + scale * chartsBounds.length());
      }

      yoLegend.updateValueFields();
      charts.values().forEach(YoVariableChartPackage::updateChart);
   }

   private ContextMenu newGraphContextMenu()
   {
      ContextMenu contextMenu = new ContextMenu();
      for (YoVariable<?> yoVariable : charts.keySet())
      {
         MenuItem menuItem = new MenuItem("Remove " + yoVariable.getName());
         menuItem.setMnemonicParsing(false);
         menuItem.setOnAction(e -> removeYoVariableFromPlot(yoVariable));
         contextMenu.getItems().add(menuItem);
      }
      return contextMenu;
   }

   private void hideContextMenu()
   {
      if (contextMenuProperty.get() != null)
         contextMenuProperty.set(null);
   }

   private void handleMousePressed(MouseEvent event)
   {
      if (event.getButton() == MouseButton.PRIMARY)
      {
         hideContextMenu();

         if (bufferPropertiesForScrolling.get() != null)
         {
            Node intersectedNode = event.getPickResult().getIntersectedNode();

            if (intersectedNode == null || intersectedNode instanceof DynamicChartLegend || intersectedNode instanceof LabeledText
                  || intersectedNode instanceof Label)
               return; // Don't perform scroll when clicking on the legend

            int index = screenToBufferIndex(event.getScreenX(), event.getScreenY());
            messager.submitMessage(topics.getYoBufferCurrentIndexRequest(), index);
            event.consume();
         }
      }
   }

   private void handleMouseMiddleClick(MouseEvent e)
   {
      if (e.isMiddleButtonDown() && e.getPickResult().getIntersectedNode() instanceof LabeledText)
      { // TODO The legend's name needs to be unique within a single graph
         String pickedName = ((LabeledText) e.getPickResult().getIntersectedNode()).getText();
         Optional<YoVariableChartPackage> chartData = charts.values().stream().filter(dataPackage -> dataPackage.series.getSeriesName().equals(pickedName))
                                                            .findFirst();
         if (chartData.isPresent())
            removeYoVariableFromPlot(chartData.get().getYoVariable());
      }
   }

   private Point2D lastMouseScreenPosition = null;

   private void handleMouseDrag(MouseEvent event)
   {
      handleMousePressed(event);

      if (event.isSecondaryButtonDown())
      {
         Point2D newMouseScreenPosition = new Point2D(event.getScreenX(), event.getScreenY());

         if (!event.isStillSincePress())
         {
            hideContextMenu();

            if (lastMouseScreenPosition != null)
            {
               int drag = screenToBufferIndex(lastMouseScreenPosition) - screenToBufferIndex(newMouseScreenPosition);
               messager.submitMessage(topics.getYoChartRequestShift(), drag);
            }
         }

         lastMouseScreenPosition = newMouseScreenPosition;
         event.consume();
      }
   }

   private void handleMouseReleased(MouseEvent event)
   {
      if (event.getButton() == MouseButton.SECONDARY)
      {
         lastMouseScreenPosition = null;

         if (event.isStillSincePress())
         {
            ContextMenu contextMenu = newGraphContextMenu();
            if (!contextMenu.getItems().isEmpty())
            {
               contextMenuProperty.set(contextMenu);
               contextMenu.show(lineChart, event.getScreenX(), event.getScreenY());
            }
            event.consume();
         }
      }
   }

   private int screenToBufferIndex(Tuple2DReadOnly screenPosition)
   {
      return screenToBufferIndex(screenPosition.getX(), screenPosition.getY());
   }

   private int screenToBufferIndex(double screenX, double screenY)
   {
      if (bufferPropertiesForScrolling.get() == null)
         return -1;
      double xLocal = xAxis.screenToLocal(screenX, screenY).getX();
      int index = (int) Math.round(xAxis.getValueForDisplay(xLocal));
      return MathTools.clamp(index, 0, bufferPropertiesForScrolling.get().getSize());
   }

   private void handleScroll(ScrollEvent event)
   {
      if (bufferPropertiesForScrolling.get() != null)
      {
         int scrollDelta = event.isControlDown() ? 10 : 1;
         if (event.getDeltaY() == 0.0)
            return;

         hideContextMenu();

         if (event.getDeltaY() < 0.0)
            messager.submitMessage(topics.getYoBufferDecrementCurrentIndexRequest(), scrollDelta);
         else
            messager.submitMessage(topics.getYoBufferIncrementCurrentIndexRequest(), scrollDelta);
      }
   }

   public void handleDragDetected(MouseEvent event)
   {
      if (event == null)
         return;

      if (!event.isPrimaryButtonDown())
         return;

      PickResult pickResult = event.getPickResult();

      if (pickResult == null)
         return;

      Node intersectedNode = pickResult.getIntersectedNode();

      if (intersectedNode == null)
         return;

      if (intersectedNode instanceof LabeledText)
      {
         LabeledText legend = (LabeledText) intersectedNode;
         String yoVariableName = legend.getText().split("\\s+")[0];
         YoVariable<?> yoVariableSelected = charts.keySet().stream().filter(yoVariable -> yoVariable.getName().equals(yoVariableName)).findFirst().orElse(null);
         if (yoVariableSelected == null)
            return;
         Dragboard dragBoard = legend.startDragAndDrop(TransferMode.ANY);
         ClipboardContent clipboardContent = new ClipboardContent();
         clipboardContent.put(DragAndDropTools.YO_COMPOSITE_REFERENCE,
                              Arrays.asList(YoCompositeTools.YO_VARIABLE, yoVariableSelected.getFullNameWithNameSpace()));
         dragBoard.setContent(clipboardContent);
      }

      event.consume();
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         lineChart.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         lineChart.setStyle("-fx-border-color: null;");
   }

   public void handleDragEntered(DragEvent event)
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
      if (event.isAccepted())
         return;

      Dragboard db = event.getDragboard();
      boolean success = false;
      List<YoComposite> yoComposites = DragAndDropTools.retrieveYoCompositesFromDragBoard(db, yoCompositeSearchManager);
      if (yoComposites != null)
      {
         for (YoComposite yoComposite : yoComposites)
            yoComposite.getYoComponents().forEach(this::addYoVariableToPlot);
         success = true;
      }
      event.setDropCompleted(success);
      event.consume();
   }

   private boolean acceptDragEventForDrop(DragEvent event)
   {
      if (event.getGestureSource() == lineChart)
         return false;

      Dragboard dragboard = event.getDragboard();
      return DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoCompositeSearchManager) != null;
   }

   public Button getCloseButton()
   {
      return closeButton;
   }

   public AnchorPane getMainPane()
   {
      return chartMainPane;
   }

   public XYChart getLineChart()
   {
      return lineChart;
   }

   public YoChartConfigurationDefinition toYoChartConfigurationDefinition()
   {
      return toYoChartConfigurationDefinition(new ChartIdentifier(-1, -1));
   }

   public YoChartConfigurationDefinition toYoChartConfigurationDefinition(ChartIdentifier chartIdentifier)
   {
      YoChartConfigurationDefinition definition = new YoChartConfigurationDefinition();
      definition.setIdentifier(YoChartTools.toYoChartIdentifierDefinition(chartIdentifier));
      definition.setChartStyle("RAW"); // TODO Re-add chart style
      definition.setYoVariables(charts.keySet().stream().map(YoVariable::getFullNameWithNameSpace).collect(Collectors.toList()));
      definition.setYBounds(charts.values().stream().map(pack -> YoChartTools.toChartDoubleBoundsDefinition(pack.getSeries().getCustomYBounds()))
                                  .collect(Collectors.toList()));
      definition.setNegates(charts.values().stream().map(pack -> pack.getSeries().isNegated()).collect(Collectors.toList()));
      return definition;
   }

   private class YoVariableChartPackage
   {
      private final YoNumberSeries series;
      private final YoDoubleDataSet yoDataSet;
      private final YoVariableChartData<?, ?> chartData;
      private final Object callerID = YoChartPanelController.this;

      public YoVariableChartPackage(YoVariable<?> yoVariable)
      {
         series = new YoNumberSeries(yoVariable);
         chartData = chartDataManager.getYoVariableChartData(callerID, yoVariable);
         yoDataSet = new YoDoubleDataSet(yoVariable, 1);
         yoDataSet.add(0.0, 0.0);
         lineChart.getDatasets().add(yoDataSet);
      }

      private int lastUpdateEndIndex = -1;

      public void updateChart()
      {
         ChartDataUpdate newData = chartData.pollChartData(callerID);
         if (newData != null)
         {
            newData.readUpdate(yoDataSet, lastUpdateEndIndex);
            lastUpdateEndIndex = newData.getUpdateEndIndex();
         }
      }

      public YoVariable<?> getYoVariable()
      {
         return yoDataSet.getYoVariable();
      }

      public YoNumberSeries getSeries()
      {
         return series;
      }

      public void close()
      {
         lineChart.getDatasets().remove(yoDataSet);
         chartData.removeCaller(callerID);
      }
   }
}
