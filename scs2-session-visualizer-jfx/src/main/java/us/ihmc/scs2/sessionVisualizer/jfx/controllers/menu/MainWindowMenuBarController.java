package us.ihmc.scs2.sessionVisualizer.jfx.controllers.menu;

import javafx.fxml.FXML;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.VisualizerController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public class MainWindowMenuBarController implements VisualizerController
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
   private YoSliderboardMenuController yoSliderboardMenuController;
   @FXML
   private HelpMenuController helpMenuController;

   @Override
   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      fileMenuController.initialize(toolkit);
      runMenuController.initialize(toolkit);
      dataBufferMenuController.initialize(toolkit);
      yoCompositeMenuController.initialize(toolkit);
      yoChartMenuController.initialize(toolkit);
      yoGraphicMenuController.initialize(toolkit);
      sessionMenuController.initialize(toolkit);
      yoSliderboardMenuController.initialize(toolkit);
      helpMenuController.initialize(toolkit);
   }
}
