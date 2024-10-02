package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoPieChart;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoPieChartVariableController extends YoPieChartInputController
{
   @FXML
   private HBox rootPane;

   @FXML
   private Label yoVariableLabel;

   public void initialize(SessionVisualizerToolkit toolkit, PieChart pieChart)
   {
      super.initialize(toolkit, rootPane, yoVariableLabel, YoDouble.class::isInstance, pieChart);
      clear();
   }
}
