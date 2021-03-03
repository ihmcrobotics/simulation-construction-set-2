package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import javafx.fxml.FXML;
import javafx.stage.Window;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class MainWindowMenuBarController
{
   @FXML
   private FileMenuController fileMenuController;
   @FXML
   private RunMenuController runMenuController;
   @FXML
   private DataBufferMenuController dataBufferMenuController;
   @FXML
   private YoCompositeMenuController yoCompositeMenuController;
   @FXML
   private YoChartMenuController yoChartMenuController;
   @FXML
   private YoGraphicMenuController yoGraphicMenuController;
   @FXML
   private SessionMenuController sessionMenuController;
   @FXML
   private SliderboardMenuController sliderboardMenuController;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      fileMenuController.initialize(toolkit);
      runMenuController.initialize(toolkit);
      dataBufferMenuController.initialize(toolkit);
      yoCompositeMenuController.initialize(toolkit);
      yoChartMenuController.initialize(toolkit, owner);
      yoGraphicMenuController.initialize(toolkit);
      sessionMenuController.initialize(toolkit);
      sliderboardMenuController.initialize(toolkit);
   }
}
