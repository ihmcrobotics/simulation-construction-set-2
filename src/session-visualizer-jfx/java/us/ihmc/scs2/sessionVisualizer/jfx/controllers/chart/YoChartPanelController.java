package us.ihmc.scs2.sessionVisualizer.jfx.controllers.chart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.jfoenix.controls.JFXButton;
import com.sun.javafx.scene.control.skin.LabeledText;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
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
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.ChartIdentifier;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.ChartIntegerBounds;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.ChartMarker;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.DataEntry;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.DynamicChartLegend;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.DynamicLineChart;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.DynamicLineChart.ChartStyle;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.YoChartTools;
import us.ihmc.scs2.sessionVisualizer.jfx.charts.YoVariableChartData;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.BackgroundExecutorManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ChartDataManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.CompositePropertyTools.YoVariableDatabase;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;
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

   private final NumberAxis xAxis = new NumberAxis(0.0, 0.0, 1000.0);
   private final NumberAxis yAxis = new NumberAxis();
   private DynamicLineChart dynamicLineChart;

   private final Data<Number, Number> inPointMarker = new Data<>(0, 0.0);
   private final Data<Number, Number> outPointMarker = new Data<>(0, 0.0);
   private final Data<Number, Number> bufferIndexMarker = new Data<>(0, 0.0);
   private final List<Data<Number, Number>> keyFrameMarkers = new ArrayList<>();

   private YoCompositeSearchManager yoCompositeSearchManager;

   private ChartDataManager chartDataManager;
   private final ObservableList<YoNumberSeries> yoNumberSeriesList = FXCollections.observableArrayList();
   private final ObservableMap<YoVariable, YoVariableChartPackage> charts = FXCollections.observableMap(new LinkedHashMap<>());
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
      BackgroundExecutorManager backgroundExecutorManager = toolkit.getBackgroundExecutorManager();

      bufferPropertiesForMarkers = messager.createInput(topics.getYoBufferCurrentProperties());
      bufferPropertiesForScrolling = messager.createInput(topics.getYoBufferCurrentProperties());

      dynamicLineChart = new DynamicLineChart(xAxis, yAxis, backgroundExecutorManager::executeInBackground, toolkit.getChartRenderManager());
      chartMainPane.getChildren().add(0, dynamicLineChart);
      AnchorPane.setTopAnchor(dynamicLineChart, 0.0);
      AnchorPane.setBottomAnchor(dynamicLineChart, 0.0);
      AnchorPane.setLeftAnchor(dynamicLineChart, 0.0);
      AnchorPane.setRightAnchor(dynamicLineChart, 0.0);

      xAxis.setMinorTickVisible(false);
      xAxis.setTickLabelsVisible(false);
      xAxis.setTickMarkVisible(false);
      xAxis.setLowerBound(-1);
      xAxis.setAnimated(false);
      xAxis.setAutoRanging(false);

      yAxis.setMinorTickVisible(false);
      yAxis.setTickLabelsVisible(false);
      yAxis.setTickMarkVisible(false);
      yAxis.setAnimated(false);
      yAxis.setAutoRanging(true);
      yAxis.setForceZeroInRange(false);

      ChartMarker inPointMarkerNode = dynamicLineChart.addMarker(inPointMarker);
      inPointMarkerNode.getStyleClass().add(INPOINT_MARKER_STYLECLASS);
      ChartMarker outPointMarkerNode = dynamicLineChart.addMarker(outPointMarker);
      outPointMarkerNode.getStyleClass().add(OUTPOINT_MARKER_STYLECLASS);
      ChartMarker currentIndexMarkerNode = dynamicLineChart.addMarker(bufferIndexMarker);
      currentIndexMarkerNode.getStyleClass().add(CURRENT_INDEX_MARKER_STYLECLASS);

      Data<Number, Number> origin = new Data<>(0.0, 0.0);
      ChangeListener<? super ChartStyle> originMarkerUpdater = (o, oldValue, newValue) ->
      {
         if (newValue == ChartStyle.RAW)
         {
            ChartMarker originMarker = dynamicLineChart.addMarker(origin);
            originMarker.getStyleClass().add(ORIGIN_MARKER_STYLECLASS);
         }
         else
         {
            dynamicLineChart.removeMarker(origin);
         }
      };
      dynamicLineChart.chartStyleProperty().addListener(originMarkerUpdater);
      originMarkerUpdater.changed(null, null, dynamicLineChart.chartStyleProperty().get());

      dynamicLineChart.setOnDragDetected(this::handleDragDetected);
      dynamicLineChart.setOnDragOver(this::handleDragOver);
      dynamicLineChart.setOnDragDropped(this::handleDragDropped);
      dynamicLineChart.setOnDragEntered(this::handleDragEntered);
      dynamicLineChart.setOnDragExited(this::handleDragExited);
      dynamicLineChart.setOnMousePressed(this::handleMousePressed);
      dynamicLineChart.setOnMouseDragged(this::handleMouseDrag);
      dynamicLineChart.setOnMouseReleased(this::handleMouseReleased);
      dynamicLineChart.setOnScroll(this::handleScroll);
      dynamicLineChart.addEventHandler(MouseEvent.MOUSE_PRESSED, e ->
      {
         if (e.isMiddleButtonDown() && e.getPickResult().getIntersectedNode() instanceof LabeledText)
         { // TODO The legend's name needs to be unique within a single graph
            String pickedName = ((LabeledText) e.getPickResult().getIntersectedNode()).getText();
            Optional<YoVariableChartPackage> chartData = charts.values().stream().filter(dataPackage -> dataPackage.series.getSeriesName().equals(pickedName))
                                                               .findFirst();
            if (chartData.isPresent())
               removeYoVariableFromPlot(chartData.get().getYoVariable());
         }
      });
      contextMenuProperty.addListener((ChangeListener<ContextMenu>) (observable, oldValue, newValue) ->
      {
         if (oldValue != null)
            oldValue.hide();
      });
      charts.addListener((MapChangeListener<YoVariable, YoVariableChartPackage>) change ->
      {
         if (change.wasAdded())
            yoNumberSeriesList.add(change.getValueAdded().getSeries());
         else if (change.wasRemoved())
            yoNumberSeriesList.remove(change.getValueRemoved().getSeries());

         JavaFXMissingTools.runNFramesLater(1, () -> charts.values().forEach(YoVariableChartPackage::updateLegend));
      });

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
         dynamicLineChart.setChartStyle(ChartStyle.RAW);
         return;
      }

      if (definition.getChartStyle() != null)
      {
         try
         {
            dynamicLineChart.setChartStyle(ChartStyle.valueOf(definition.getChartStyle()));
         }
         catch (IllegalArgumentException e)
         {
            dynamicLineChart.setChartStyle(ChartStyle.RAW);
         }
      }
      else
      {
         dynamicLineChart.setChartStyle(ChartStyle.RAW);
      }

      for (YoVariableChartPackage pack : charts.values())
      {
         int definitionIndex = definition.getYoVariables().indexOf(pack.getYoVariable().getFullNameString());
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
         activeChartOptionControllerProperty.get().setInput(yoNumberSeriesList, dynamicLineChart.chartStyleProperty());
         activeChartOptionControllerProperty.get().showWindow();
         return;
      }

      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.CHART_OPTION_DIALOG_URL);
         loader.load();
         YoChartOptionController controller = loader.getController();
         controller.initialize(toolkit, parentWindow);
         controller.setInput(yoNumberSeriesList, dynamicLineChart.chartStyleProperty());
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
      YoVariable yoVariable = rootRegistryDatabase.searchExact(yoVariableFullName);
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

   public void addYoVariableToPlot(YoVariable yoVariable)
   {
      if (charts.containsKey(yoVariable))
         return;
      charts.put(yoVariable, new YoVariableChartPackage(yoVariable));
   }

   public void addYoVariablesToPlot(Collection<? extends YoVariable> yoVariables)
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

   public void removeYoVariableFromPlot(YoVariable yoVariable)
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
      keyFrameMarkers.forEach(marker -> dynamicLineChart.removeMarker(marker));
      keyFrameMarkers.clear();

      for (int keyFrame : newKeyFrames)
      {
         Data<Number, Number> marker = new Data<>(keyFrame, 0);
         keyFrameMarkers.add(marker);
         ChartMarker keyFrameMarkerNode = dynamicLineChart.addMarker(marker);
         keyFrameMarkerNode.getStyleClass().add(KEYFRAME_MARKER_STYLECLASS);
      }
   }

   @Override
   public void handle(long now)
   {
      ChartIntegerBounds chartsBounds = chartDataManager.chartBoundsProperty().getValue();
      YoBufferPropertiesReadOnly bufferProperties = bufferPropertiesForMarkers.getAndSet(null);

      if (bufferProperties != null)
      {
         if (bufferProperties.getInPoint() != inPointMarker.getXValue().intValue())
            inPointMarker.setXValue(bufferProperties.getInPoint());
         if (bufferProperties.getOutPoint() != outPointMarker.getXValue().intValue())
            outPointMarker.setXValue(bufferProperties.getOutPoint());
         if (bufferProperties.getCurrentIndex() != bufferIndexMarker.getXValue().intValue())
            bufferIndexMarker.setXValue(bufferProperties.getCurrentIndex());
         if (chartsBounds == null)
         {
            double scale = 0.001;
            xAxis.setLowerBound(-scale * bufferProperties.getSize());
            xAxis.setUpperBound((1.0 + scale) * bufferProperties.getSize());
            xAxis.setMinorTickLength(0);
         }

         charts.values().forEach(YoVariableChartPackage::updateLegend);

         bufferProperties = null;
      }

      if (chartsBounds != null)
      {
         double scale = 0.001;
         xAxis.setLowerBound(chartsBounds.getLower() - scale * chartsBounds.length());
         xAxis.setUpperBound(chartsBounds.getUpper() + scale * chartsBounds.length());
      }

      charts.values().forEach(YoVariableChartPackage::updateChart);
   }

   private ContextMenu newGraphContextMenu()
   {
      ContextMenu contextMenu = new ContextMenu();
      for (YoVariable yoVariable : charts.keySet())
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
               contextMenu.show(dynamicLineChart, event.getScreenX(), event.getScreenY());
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
      int index = Math.round(xAxis.getValueForDisplay(xLocal).floatValue());
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
         YoVariable yoVariableSelected = charts.keySet().stream().filter(yoVariable -> yoVariable.getName().equals(yoVariableName)).findFirst().orElse(null);
         if (yoVariableSelected == null)
            return;
         Dragboard dragBoard = legend.startDragAndDrop(TransferMode.ANY);
         ClipboardContent clipboardContent = new ClipboardContent();
         clipboardContent.put(DragAndDropTools.YO_COMPOSITE_REFERENCE,
                              Arrays.asList(YoCompositeTools.YO_VARIABLE, yoVariableSelected.getFullNameString()));
         dragBoard.setContent(clipboardContent);
      }

      event.consume();
   }

   public void setSelectionHighlight(boolean isSelected)
   {
      if (isSelected)
         dynamicLineChart.setStyle("-fx-border-color:green; -fx-border-radius:5;");
      else
         dynamicLineChart.setStyle("-fx-border-color: null;");
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
      if (event.getGestureSource() == dynamicLineChart)
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

   public DynamicLineChart getLineChart()
   {
      return dynamicLineChart;
   }

   public YoChartConfigurationDefinition toYoChartConfigurationDefinition()
   {
      return toYoChartConfigurationDefinition(new ChartIdentifier(-1, -1));
   }

   public YoChartConfigurationDefinition toYoChartConfigurationDefinition(ChartIdentifier chartIdentifier)
   {
      YoChartConfigurationDefinition definition = new YoChartConfigurationDefinition();
      definition.setIdentifier(YoChartTools.toYoChartIdentifierDefinition(chartIdentifier));
      definition.setChartStyle(dynamicLineChart.getChartStyle().name());
      definition.setYoVariables(charts.keySet().stream().map(YoVariable::getFullNameString).collect(Collectors.toList()));
      definition.setYBounds(charts.values().stream().map(pack -> YoChartTools.toChartDoubleBoundsDefinition(pack.getSeries().getCustomYBounds()))
                                  .collect(Collectors.toList()));
      definition.setNegates(charts.values().stream().map(pack -> pack.getSeries().isNegated()).collect(Collectors.toList()));
      return definition;
   }

   private class YoVariableChartPackage
   {
      private final YoNumberSeries series;
      private final YoVariableChartData<?, ?> chartData;
      private final Object callerID = YoChartPanelController.this;

      public YoVariableChartPackage(YoVariable yoVariable)
      {
         series = new YoNumberSeries(yoVariable);
         chartData = chartDataManager.getYoVariableChartData(callerID, yoVariable);
         dynamicLineChart.addSeries(series);
      }

      public void updateLegend()
      {
         series.updateLegend();
      }

      public void updateChart()
      {
         DataEntry newData = chartData.pollChartData(callerID);
         if (newData != null)
            series.setDataEntry(newData);
      }

      public YoVariable getYoVariable()
      {
         return series.getYoVariable();
      }

      public YoNumberSeries getSeries()
      {
         return series;
      }

      public void close()
      {
         dynamicLineChart.removeSeries(series);
         chartData.removeCaller(callerID);
      }
   }
}