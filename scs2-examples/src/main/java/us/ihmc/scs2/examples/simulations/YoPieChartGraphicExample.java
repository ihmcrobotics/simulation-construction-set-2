package us.ihmc.scs2.examples.simulations;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import us.ihmc.scs2.SimulationConstructionSet2;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoPieChart.YoPieChartController;
import us.ihmc.yoVariables.variable.YoDouble;

import java.io.IOException;

public class YoPieChartGraphicExample
{
   public static void main(String[] args)
   {
      SimulationConstructionSet2 scs = new SimulationConstructionSet2(SimulationConstructionSet2.doNothingPhysicsEngine());
      YoDouble[] array = new YoDouble[10];
      for (int i = 0; i < array.length; i++)
      {
         array[i] = new YoDouble("yoDouble" + i, scs.getRootRegistry());
         array[i].set(Math.random());
      }

      scs.start(true, false, false);

      Platform.runLater(() ->
                        {
//                           try
//                           {
//                              Parent root = FXMLLoader.load(YoPieChartGraphicExample.class.getResource("/fxml/yoPieChart/YoPieChartWindow.fxml"));
//                           }
//                           catch (IOException e)
//                           {
//                              throw new RuntimeException(e);
//                           }

                           //                           YoPieChartController yoPieChartController = null;
//                           try
//                           {
//                              yoPieChartController = new YoPieChartController();
//                           }
//                           catch (IOException e)
//                           {
//                              throw new RuntimeException(e);
//                           }
//                           scs.addVisualizerShutdownListener(yoPieChartController.getWindow()::close);
                        });
   }
}
