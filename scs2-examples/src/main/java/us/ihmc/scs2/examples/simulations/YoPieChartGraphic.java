package us.ihmc.scs2.examples.simulations;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import us.ihmc.scs2.SimulationConstructionSet2;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoPieChartGraphic
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
                           Stage window = new Stage();
                           window.setTitle("YoPieChartGraphic");
                           YoPieChart yoPieChart = new YoPieChart();
                           yoPieChart.arrayProperty.setValue(array);
                           yoPieChart.animationTimer.start();
                           window.setScene(new Scene(yoPieChart, 600, 400));

                           window.setOnCloseRequest(e -> yoPieChart.animationTimer.stop());
                           scs.addVisualizerShutdownListener(window::close);
                           window.show();
                        });
   }

   private static class YoPieChart extends PieChart
   {
      private final Property<YoDouble[]> arrayProperty = new SimpleObjectProperty<>(this, "array", null);
      private final AnimationTimer animationTimer;

      public YoPieChart()
      {
         setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
         setStyle("-fx-background-color: grey;");

         for (int i = 0; i < 10; i++)
         {
            getData().add(new Data("yoDouble" + i, Math.random()));
         }

         animationTimer = new AnimationTimer()
         {
            @Override
            public void handle(long now)
            {
               YoDouble[] array = arrayProperty.getValue();
               if (array == null)
                  return;

               for (int i = 0; i < array.length; i++)
               {
                  YoDouble yoDouble = array[i];
                  getData().get(i).setPieValue(yoDouble.getValue());
               }
            }
         };
      }
   }
}
