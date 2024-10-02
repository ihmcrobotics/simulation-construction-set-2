package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoPieChart;

import com.jfoenix.controls.JFXToggleNode;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.VisualizerController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;

import java.util.Arrays;
import java.util.List;

public class YoPieChartController implements VisualizerController
{
   @FXML
   private JFXToggleNode button;

   @FXML
   private YoPieChartVariableController variable0Controller, variable1Controller, variable2Controller, variable3Controller, variable4Controller, variable5Controller;

   private List<YoPieChartVariableController> variableControllers;


   @FXML
   private PieChart pieChart;

   @FXML
   private Stage stage;

   @Override
   public void initialize(SessionVisualizerWindowToolkit toolkit)
   {
      variableControllers = Arrays.asList(variable0Controller, variable1Controller, variable2Controller, variable3Controller, variable4Controller, variable5Controller);

      for (int i = 0; i < variableControllers.size(); i++)
      {
         YoPieChartVariableController variableController = variableControllers.get(i);
         variableController.initialize(toolkit.getGlobalToolkit(), pieChart);
      }

      stage.initOwner(toolkit.getWindow());
      stage.show();
      JavaFXMissingTools.centerWindowInOwner(stage, toolkit.getWindow());
   }
}
