package us.ihmc.scs2.sessionVisualizer.controllers.chart;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.jfoenix.controls.JFXButton;

import de.gsi.chart.XYChart;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Pair;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.messager.TopicListener;
import us.ihmc.scs2.definition.yoChart.YoChartConfigurationDefinition;
import us.ihmc.scs2.definition.yoChart.YoChartGroupConfigurationDefinition;
import us.ihmc.scs2.session.SessionState;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.charts.ChartGroupLayout;
import us.ihmc.scs2.sessionVisualizer.charts.ChartGroupModel;
import us.ihmc.scs2.sessionVisualizer.charts.ChartGroupTools;
import us.ihmc.scs2.sessionVisualizer.charts.ChartIdentifier;
import us.ihmc.scs2.sessionVisualizer.controllers.TableSizeQuickAccess;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoComposite;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoChartGroupPanelController
{
   private SessionVisualizerToolkit toolkit;
   private YoCompositeSearchManager yoCompositeSearchManager;

   private final IntegerProperty numberOfRows = new SimpleIntegerProperty(this, "numberOfRows", 0);
   private final IntegerProperty numberOfCols = new SimpleIntegerProperty(this, "numberOfCols", 0);
   private final GridPane gridPane = new GridPane();
   private final Collection<YoChartPanelController> chartControllers = new ConcurrentLinkedQueue<>();
   private final BooleanProperty isRunning = new SimpleBooleanProperty(this, "isRunning", false);

   private final IntegerProperty maximumRowNumberProperty = new SimpleIntegerProperty(this, "maximumRow", 3);
   private final IntegerProperty maximumColNumberProperty = new SimpleIntegerProperty(this, "maximumColumn", 4);

   @FXML
   private AnchorPane mainPane;
   @FXML
   private JFXButton dropDownMenuButton;

   private TopicListener<Pair<Window, File>> loadChartGroupConfigurationListener = m -> loadChartGroupConfiguration(m.getKey(), m.getValue());
   private TopicListener<Pair<Window, File>> saveChartGroupConfigurationListener = m -> saveChartGroupConfiguration(m.getKey(), m.getValue());
   private TopicListener<SessionState> stopSessionListener = state ->
   {
      if (state == SessionState.INACTIVE)
         clear();
   };

   private Window owner;
   private SessionVisualizerTopics topics;
   private JavaFXMessager messager;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      this.toolkit = toolkit;
      this.owner = owner;
      yoCompositeSearchManager = toolkit.getYoCompositeSearchManager();

      mainPane.getChildren().add(0, gridPane);
      AnchorPane.setTopAnchor(gridPane, 0.0);
      AnchorPane.setLeftAnchor(gridPane, 0.0);
      AnchorPane.setRightAnchor(gridPane, 0.0);
      AnchorPane.setBottomAnchor(gridPane, 0.0);
      gridPane.getStyleClass().add("chart-group-grid-pane");

      messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      messager.submitMessage(topics.getRegisterRecordable(), mainPane);

      messager.registerJavaFXSyncedTopicListener(topics.getYoChartGroupLoadConfiguration(), loadChartGroupConfigurationListener);
      messager.registerJavaFXSyncedTopicListener(topics.getYoChartGroupSaveConfiguration(), saveChartGroupConfigurationListener);
      messager.registerJavaFXSyncedTopicListener(topics.getSessionCurrentState(), stopSessionListener);
   }

   public void setChartGroupConfiguration(YoChartGroupConfigurationDefinition definition)
   {
      if (definition.getNumberOfRows() > maximumRowNumberProperty.get() || definition.getNumberOfColumns() > maximumColNumberProperty.get())
      {
         LogTools.warn("Cannot set from configuration, required number of rows/columns is too large.");
         return;
      }

      clear();
      numberOfRows.set(definition.getNumberOfRows());
      numberOfCols.set(definition.getNumberOfColumns());
      updateChartLayout();
      if (definition.getChartConfigurations() != null)
      {
         for (YoChartConfigurationDefinition chartConfiguration : definition.getChartConfigurations())
         {
            YoChartPanelController chartController = getChartController(new ChartIdentifier(chartConfiguration.getIdentifier()));
            chartController.setChartConfiguration(chartConfiguration);
         }
      }
   }

   private void clear()
   {
      new ArrayList<>(chartControllers).forEach(this::closeChart);
      numberOfRows.set(0);
      numberOfCols.set(0);
   }

   public void scheduleMessagerCleanup()
   {
      toolkit.getBackgroundExecutorManager().executeInBackground(() ->
      {
         messager.removeJavaFXSyncedTopicListener(topics.getYoChartGroupLoadConfiguration(), loadChartGroupConfigurationListener);
         messager.removeJavaFXSyncedTopicListener(topics.getYoChartGroupSaveConfiguration(), saveChartGroupConfigurationListener);
         messager.removeJavaFXSyncedTopicListener(topics.getSessionCurrentState(), stopSessionListener);
      });
   }

   private void updateChartLayout()
   {
      int preferredNumberOfCharts = numberOfRows.get() * numberOfCols.get();

      if (chartControllers.size() > preferredNumberOfCharts)
      {
         int excess = chartControllers.size() - preferredNumberOfCharts;

         ArrayDeque<YoChartPanelController> emptyCharts = chartControllers.stream().filter(YoChartPanelController::isEmpty)
                                                                          .collect(Collectors.toCollection(ArrayDeque::new));

         if (!emptyCharts.isEmpty())
         {
            while (emptyCharts.size() > excess)
               emptyCharts.removeFirst();

            emptyCharts.forEach(this::closeChart);
         }
      }

      if (chartControllers.size() > preferredNumberOfCharts)
      { // Increase the number of rows to prevent graph deletion
         int excess = chartControllers.size() - preferredNumberOfCharts;
         int numberOfRowsToAdd = (excess / numberOfCols.get()) + 1;
         numberOfRows.set(numberOfRows.get() + numberOfRowsToAdd);
         preferredNumberOfCharts = numberOfRows.get() * numberOfCols.get();
      }

      while (chartControllers.size() < preferredNumberOfCharts)
      {
         YoChartPanelController newController = createNewChartPanel(isRunning.get());
         gridPane.getChildren().add(newController.getMainPane());
         chartControllers.add(newController);
      }

      updateConstraint();
   }

   private void updateConstraint()
   {
      int row = 0;
      int col = 0;

      for (YoChartPanelController chart : chartControllers)
      {
         GridPane.setConstraints(chart.getMainPane(), col, row);
         col++;
         if (col == numberOfCols.get())
         {
            row++;
            col = 0;
         }
      }
   }

   private void removeEmptyRowsAndColumns()
   {
      if (chartControllers.isEmpty())
         return;

      int lastRow = chartControllers.stream().mapToInt(chart -> GridPane.getRowIndex(chart.getMainPane()).intValue()).max().getAsInt();
      int lastColumn = chartControllers.stream().mapToInt(chart -> GridPane.getColumnIndex(chart.getMainPane()).intValue()).max().getAsInt();

      for (int row = lastRow; row >= 0; row--)
      {
         int rowFinal = row;
         boolean isEmpty = chartControllers.stream().noneMatch(chart -> GridPane.getRowIndex(chart.getMainPane()) == rowFinal);

         if (isEmpty)
         {
            for (YoChartPanelController chart : chartControllers)
            {
               int chartRow = GridPane.getRowIndex(chart.getMainPane());
               if (chartRow > row)
                  GridPane.setRowIndex(chart.getMainPane(), chartRow - 1);
            }
         }
      }

      for (int column = lastColumn; column >= 0; column--)
      {
         int columnFinal = column;
         boolean isEmpty = chartControllers.stream().noneMatch(chart -> GridPane.getColumnIndex(chart.getMainPane()) == columnFinal);

         if (isEmpty)
         {
            for (YoChartPanelController chart : chartControllers)
            {
               int chartColumn = GridPane.getColumnIndex(chart.getMainPane());
               if (chartColumn > column)
                  GridPane.setColumnIndex(chart.getMainPane(), chartColumn - 1);
            }
         }
      }
   }

   private void handleCloseChart(ActionEvent event, YoChartPanelController chartToClose)
   {
      closeChart(chartToClose);
      event.consume();
   }

   private void closeChart(YoChartPanelController chartToClose)
   {
      chartToClose.stop();
      chartToClose.close();
      gridPane.getChildren().remove(chartToClose.getMainPane());
      chartControllers.remove(chartToClose);
      removeEmptyRowsAndColumns();
      numberOfRows.set(chartControllers.size() / numberOfCols.intValue());
   }

   private void closeEmptyCharts()
   {
      Iterator<YoChartPanelController> chartControllersIterator = chartControllers.iterator();

      while (chartControllersIterator.hasNext())
      {
         YoChartPanelController controller = chartControllersIterator.next();
         if (controller.isEmpty())
         {
            chartControllersIterator.remove();
            closeChart(controller);
         }
      }
   }

   public YoChartPanelController getChartController(ChartIdentifier chartIdentifier)
   {
      return getChartController(chartIdentifier.getRow(), chartIdentifier.getColumn());
   }

   public YoChartPanelController getChartController(int row, int column)
   {
      for (YoChartPanelController controller : chartControllers)
      {
         AnchorPane node = controller.getMainPane();
         if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column)
            return controller;
      }

      return null;
   }

   public ChartIdentifier getChartIdentifier(YoChartPanelController controller)
   {
      return new ChartIdentifier(GridPane.getRowIndex(controller.getMainPane()), GridPane.getColumnIndex(controller.getMainPane()));
   }

   private YoChartPanelController createNewChartPanel(boolean start)
   {
      try
      {
         FXMLLoader loader = new FXMLLoader(SessionVisualizerIOTools.CHART_PANEL_FXML_URL);
         AnchorPane graphNode = loader.load();
         YoChartPanelController controller = loader.getController();
         controller.initialize(toolkit, owner);
         XYChart chartNode = controller.getLineChart();
         chartNode.setOnDragOver(e -> handleDragOver(e, controller));
         chartNode.setOnDragDropped(e -> handleDragDropped(e, controller));
         chartNode.setOnDragEntered(e -> handleDragEntered(e, controller));
         chartNode.setOnDragExited(e -> handleDragExited(e, controller));

         controller.getCloseButton().setOnAction(e -> handleCloseChart(e, controller));

         setGrowProperty(graphNode);

         if (start)
            controller.start();

         return controller;
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not create new line chart panel:", e);
      }
   }

   private static void setGrowProperty(Node graphNode)
   {
      GridPane.setHgrow(graphNode, Priority.ALWAYS);
      GridPane.setVgrow(graphNode, Priority.ALWAYS);
   }

   public void start()
   {
      isRunning.set(true);
      chartControllers.forEach(YoChartPanelController::start);
   }

   public void stop()
   {
      isRunning.set(false);
      chartControllers.forEach(YoChartPanelController::stop);
   }

   @FXML
   void openMenu(ActionEvent event)
   {
      Popup popup = new Popup();
      popup.autoHideProperty().set(true);
      TableSizeQuickAccess tableSizeQuickAccess = new TableSizeQuickAccess("Select graph table size:",
                                                                           maximumRowNumberProperty.get(),
                                                                           maximumColNumberProperty.get());
      AnchorPane rootNode = tableSizeQuickAccess.getMainPane();
      Pane backgroundPane = new Pane();
      backgroundPane.setStyle("-fx-background-radius:10;-fx-background-color:rgba(79,132,186,0.5);"); // TODO Needs to be extracted to CSS
      backgroundPane.setEffect(new GaussianBlur(10));
      rootNode.getChildren().add(0, backgroundPane);
      AnchorPane.setTopAnchor(backgroundPane, -5.0);
      AnchorPane.setBottomAnchor(backgroundPane, -5.0);
      AnchorPane.setLeftAnchor(backgroundPane, -5.0);
      AnchorPane.setRightAnchor(backgroundPane, -5.0);
      rootNode.setOnMouseClicked(e ->
      {
         numberOfRows.set(tableSizeQuickAccess.selectedRowsProperty().get());
         numberOfCols.set(tableSizeQuickAccess.selectedColumnsProperty().get());
         updateChartLayout();
         popup.hide();
      });
      tableSizeQuickAccess.getClearAllButton().setOnMouseClicked(e ->
      {
         clear();
         popup.hide();
      });
      tableSizeQuickAccess.getClearEmptyButton().setOnMouseClicked(e ->
      {
         closeEmptyCharts();
         popup.hide();
      });
      popup.getContent().add(rootNode);
      Bounds boundsInLocal = dropDownMenuButton.getBoundsInLocal();
      Point2D anchorPosition = dropDownMenuButton.localToScreen(boundsInLocal.getMinX(), boundsInLocal.getMaxY());

      popup.show(dropDownMenuButton, anchorPosition.getX(), anchorPosition.getY());
      event.consume();
   }

   public IntegerProperty maximumRowProperty()
   {
      return maximumRowNumberProperty;
   }

   public IntegerProperty maximumColProperty()
   {
      return maximumColNumberProperty;
   }

   private void handleDragEntered(DragEvent event, YoChartPanelController controller)
   {
      if (acceptDragEventForDrop(event))
      {
         List<YoComposite> yoComposites = DragAndDropTools.retrieveYoCompositesFromDragBoard(event.getDragboard(), yoCompositeSearchManager);
         List<ChartGroupLayout> configurations = ChartGroupTools.toChartGroupLayouts(yoComposites);
         configurations = shiftConfigurationsToSelectedChart(controller, configurations);
         List<YoChartPanelController> controllers = controllersInConfigurations(configurations);
         controllers.forEach(c -> c.setSelectionHighlight(true));
      }
      event.consume();
   }

   private void handleDragExited(DragEvent event, YoChartPanelController controller)
   {
      if (acceptDragEventForDrop(event))
      {
         chartControllers.forEach(c -> c.setSelectionHighlight(false));
      }
      event.consume();
   }

   private void handleDragOver(DragEvent event, YoChartPanelController controller)
   {
      if (acceptDragEventForDrop(event))
      {
         event.acceptTransferModes(TransferMode.ANY);
      }
      event.consume();
   }

   private void handleDragDropped(DragEvent event, YoChartPanelController controller)
   {
      boolean success = false;
      List<YoComposite> yoComposites = DragAndDropTools.retrieveYoCompositesFromDragBoard(event.getDragboard(), yoCompositeSearchManager);

      if (yoComposites != null)
      {
         List<ChartGroupLayout> layouts = ChartGroupTools.toChartGroupLayouts(yoComposites);
         layouts = shiftConfigurationsToSelectedChart(controller, layouts);

         if (layouts.size() > 1)
         {
            ContextMenu contextMenu = new ContextMenu();

            for (ChartGroupLayout layout : layouts)
            {
               Label label = new Label(layout.getName());
               CustomMenuItem menuItem = new CustomMenuItem(label);

               label.setOnMouseEntered(e ->
               {
                  chartControllers.forEach(c -> c.setSelectionHighlight(false));
                  controllersInConfiguration(layout).forEach(c -> c.setSelectionHighlight(true));
               });

               menuItem.setOnAction(e -> applyLayout(layout));
               contextMenu.getItems().add(menuItem);
            }

            contextMenu.show(mainPane, event.getScreenX(), event.getScreenY());
         }
         else if (layouts.size() == 1)
         {
            applyLayout(layouts.get(0));
         }
         else
         {
            yoComposites.forEach(yoComposite -> yoComposite.getYoComponents().forEach(controller::addYoVariableToPlot));
         }

         success = true;
      }
      event.setDropCompleted(success);
      event.consume();
   }

   private void applyLayout(ChartGroupLayout layout)
   {
      chartControllers.forEach(c -> c.setSelectionHighlight(false));

      for (ChartIdentifier chartIdentifier : layout.getChartIdentifiers())
      {
         List<? extends YoVariable<?>> yoVariables = layout.getYoVariables(chartIdentifier);
         YoChartPanelController chartController = getChartController(chartIdentifier);
         chartController.addYoVariablesToPlot(yoVariables);
      }
   }

   private List<YoChartPanelController> controllersInConfiguration(ChartGroupModel configuration)
   {
      if (!doesConfigurationFit(configuration))
         return null;
      else
         return configuration.getChartIdentifiers().stream().map(this::getChartController).collect(Collectors.toList());
   }

   public List<YoChartPanelController> controllersInConfigurations(List<? extends ChartGroupModel> configurations)
   {
      return configurations.stream().flatMap(config -> config.getChartIdentifiers().stream()).distinct().map(this::getChartController)
                           .collect(Collectors.toList());
   }

   private List<ChartGroupLayout> shiftConfigurationsToSelectedChart(YoChartPanelController selectedChart, List<ChartGroupLayout> layouts)
   {
      ChartIdentifier selectedId = getChartIdentifier(selectedChart);
      return layouts.stream().map(config -> config.shift(selectedId.getRow(), selectedId.getColumn())).filter(this::doesConfigurationFit)
                    .collect(Collectors.toList());

   }

   private boolean acceptDragEventForDrop(DragEvent event)
   {
      if (event.getGestureSource() == mainPane)
         return false;

      return DragAndDropTools.retrieveYoCompositesFromDragBoard(event.getDragboard(), yoCompositeSearchManager) != null;
   }

   private boolean doesConfigurationFit(ChartGroupModel configuration)
   {
      return configuration.rowEnd() < numberOfRows.get() && configuration.columnEnd() < numberOfCols.get();
   }

   public void loadChartGroupConfiguration(Window source, File file)
   {
      if (source != owner)
         return;

      LogTools.info("Loading file: " + file);

      try
      {
         JAXBContext context = JAXBContext.newInstance(YoChartGroupConfigurationDefinition.class);
         Unmarshaller unmarshaller = context.createUnmarshaller();
         setChartGroupConfiguration((YoChartGroupConfigurationDefinition) unmarshaller.unmarshal(file));
      }
      catch (JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void saveChartGroupConfiguration(Window source, File file)
   {
      if (source != owner)
         return;

      if (!Platform.isFxApplicationThread())
         throw new IllegalStateException("Save must only be used from the FX Application Thread");

      LogTools.info("Saving file: " + file);

      try
      {
         JAXBContext context = JAXBContext.newInstance(YoChartGroupConfigurationDefinition.class);
         Marshaller marshaller = context.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(toYoChartGroupConfigurationDefinition(), file);
      }
      catch (JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public YoChartGroupConfigurationDefinition toYoChartGroupConfigurationDefinition()
   {
      YoChartGroupConfigurationDefinition definition = new YoChartGroupConfigurationDefinition();
      definition.setNumberOfRows(numberOfRows.get());
      definition.setNumberOfColumns(numberOfCols.get());
      definition.setChartConfigurations(chartControllers.stream().map(chart -> chart.toYoChartConfigurationDefinition(getChartIdentifier(chart)))
                                                        .collect(Collectors.toList()));
      return definition;
   }
}
