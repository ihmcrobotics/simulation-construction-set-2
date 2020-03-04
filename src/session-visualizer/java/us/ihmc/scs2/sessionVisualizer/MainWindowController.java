package us.ihmc.scs2.sessionVisualizer;

import java.time.Duration;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.events.JFXDrawerEvent;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import us.ihmc.commons.Conversions;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.controllers.SessionAdvancedControlsController;
import us.ihmc.scs2.sessionVisualizer.controllers.SessionSimpleControlsController;
import us.ihmc.scs2.sessionVisualizer.controllers.chart.YoChartGroupPanelController;
import us.ihmc.scs2.sessionVisualizer.controllers.menu.MainWindowMenuBarController;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.plotter.Plotter2D;

public class MainWindowController extends AnimationTimer
{
   @FXML
   private AnchorPane mainPane;
   @FXML
   private AnchorPane sceneAnchorPane;
   @FXML
   private SplitPane mainViewSplitPane;
   @FXML
   private JFXHamburger hamburger;
   @FXML
   private JFXDrawer toolDrawer;
   @FXML
   private Label fpsLabel;
   @FXML
   private MainWindowMenuBarController mainWindowMenuBarController;
   @FXML
   private SessionSimpleControlsController sessionSimpleControlsController;
   @FXML
   private SessionAdvancedControlsController sessionAdvancedControlsController;
   @FXML
   private YoChartGroupPanelController yoChartGroupPanelController;
   private SessionVisualizerToolkit toolkit;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.toolkit = toolkit;
      Stage mainWindow = toolkit.getMainWindow();
      mainWindowMenuBarController.initialize(toolkit, mainWindow);
      sessionSimpleControlsController.initialize(toolkit);
      sessionAdvancedControlsController.initialize(toolkit);
      yoChartGroupPanelController.initialize(toolkit, mainWindow);

   }

   public void setupViewport3D(Pane viewportPane)
   {
      sceneAnchorPane.getChildren().set(0, viewportPane);
      AnchorPane.setTopAnchor(viewportPane, 0.0);
      AnchorPane.setRightAnchor(viewportPane, 0.0);
      AnchorPane.setBottomAnchor(viewportPane, 0.0);
      AnchorPane.setLeftAnchor(viewportPane, 0.0);
      toolkit.getSnapshotManager().registerRecordable(viewportPane);
   }

   private Property<Boolean> showOverheadPlotterProperty;

   public Property<Boolean> setupPlotter2D(Plotter2D plotter2D)
   {
      SubScene plotter2DScene = new SubScene(plotter2D, 100, 10);
      Pane pane = new Pane(plotter2DScene);
      plotter2DScene.heightProperty().bind(pane.heightProperty());
      plotter2DScene.widthProperty().bind(pane.widthProperty());
      plotter2D.getRoot().getChildren().add(toolkit.getYoGraphicFXManager().getRootNode2D());

      JavaFXMessager messager = toolkit.getMessager();
      showOverheadPlotterProperty = messager.createPropertyInput(toolkit.getTopics().getShowOverheadPlotter(), false);
      showOverheadPlotterProperty.addListener((o, oldValue, newValue) ->
      {
         if (newValue)
         {
            if (!mainViewSplitPane.getItems().contains(pane))
            {
               mainViewSplitPane.getItems().add(pane);
               plotter2D.setScale(400.0 / 5.0);
            }
         }
         else
         {
            mainViewSplitPane.getItems().remove(pane);
         }
      });
      return showOverheadPlotterProperty;
   }

   public void setupDrawer(Pane sidePane)
   {
      // Workaround for the drawer resizing:
      // Here we make an edge similar to a SplitPane separator on which the cursor will change
      StackPane edge = new StackPane();
      edge.setStyle("-fx-background-color:white;-fx-effect:innershadow(three-pass-box, #A9A9A9, 2.5, 0.0, -1.0, 0.0)");
      edge.setPrefWidth(5.0);
      edge.setMinWidth(5.0);
      edge.setCursor(Cursor.E_RESIZE);
      HBox hBox = new HBox(sidePane, edge);
      toolDrawer.setSidePane(hBox);
      HBox.setHgrow(sidePane, Priority.ALWAYS);
      HBox.setHgrow(edge, Priority.NEVER);
      // The edge passes on the MouseEvents used for resizing the drawer to the inner StackPane.
      edge.addEventHandler(MouseEvent.MOUSE_PRESSED, hBox.getParent()::fireEvent);
      edge.addEventHandler(MouseEvent.MOUSE_DRAGGED, hBox.getParent()::fireEvent);
      edge.addEventHandler(MouseEvent.MOUSE_RELEASED, hBox.getParent()::fireEvent);

      Node remove = toolDrawer.getChildren().remove(toolDrawer.getChildren().size() - 1);
      mainPane.addEventHandler(KeyEvent.KEY_PRESSED, (EventHandler<? super KeyEvent>) e ->
      {
         if (e.getCode() == KeyCode.ESCAPE && toolDrawer.isOpened())
         {
            toolDrawer.close();
            e.consume();
         }
      });
      toolDrawer.setResizeContent(true);
      toolDrawer.setResizableOnDrag(true);
      toolDrawer.setContent(remove);

      hamburger.setOnMouseClicked(e ->
      {
         if (toolDrawer.isClosed() || toolDrawer.isClosing())
            toolDrawer.open();
         else
            toolDrawer.close();
      });

      HamburgerSlideCloseTransition transition = new HamburgerSlideCloseTransition(hamburger);

      toolDrawer.addEventHandler(JFXDrawerEvent.CLOSING, e ->
      {
         transition.setRate(-0.5);
         transition.play();
      });
      toolDrawer.addEventFilter(JFXDrawerEvent.OPENING, e ->
      {
         transition.setRate(0.5);
         transition.play();
      });
   }

   private long timeLast = -1;
   private long timeIntervalBetweenUpdates = Duration.ofMillis(500).toNanos();
   private int frameCounter = 0;
   private double goodFPSLowerThreshold = 40.0;
   private double mediumFPSLowerThreshold = 20.0;
   private Color goodFPSColor = Color.FORESTGREEN;
   private Color mediumFPSColor = Color.DARKORANGE;
   private Color poorFPSColor = Color.RED;

   @Override
   public void handle(long timeNow)
   {
      if (timeLast == -1)
      {
         timeLast = timeNow;
         frameCounter = 0;
         return;
      }

      frameCounter++;

      if (timeNow - timeLast < timeIntervalBetweenUpdates)
         return;

      double framesPerSecond = frameCounter / Conversions.nanosecondsToSeconds(timeNow - timeLast);
      fpsLabel.setText(String.format("%6.2f FPS", framesPerSecond));
      if (framesPerSecond >= goodFPSLowerThreshold)
         fpsLabel.setTextFill(goodFPSColor);
      else if (framesPerSecond >= mediumFPSLowerThreshold)
         fpsLabel.setTextFill(mediumFPSColor);
      else
         fpsLabel.setTextFill(poorFPSColor);

      timeLast = timeNow;
      frameCounter = 0;
   }

   @Override
   public void start()
   {
      super.start();
      yoChartGroupPanelController.start();
   }

   @Override
   public void stop()
   {
      super.stop();
      yoChartGroupPanelController.stop();
   }

   public AnchorPane getMainPane()
   {
      return mainPane;
   }

   public AnchorPane getSceneAnchorPane()
   {
      return sceneAnchorPane;
   }

   public SplitPane getMainViewSplitPane()
   {
      return mainViewSplitPane;
   }

   public YoChartGroupPanelController getYoChartGroupPanelController()
   {
      return yoChartGroupPanelController;
   }

   public Property<Boolean> showOverheadPlotterProperty()
   {
      return showOverheadPlotterProperty;
   }

   public BooleanProperty showAdvancedControlsProperty()
   {
      return sessionAdvancedControlsController.showProperty();
   }
}
