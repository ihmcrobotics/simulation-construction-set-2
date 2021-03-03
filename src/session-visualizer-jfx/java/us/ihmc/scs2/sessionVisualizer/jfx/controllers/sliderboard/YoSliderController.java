package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public class YoSliderController
{
   @FXML
   private JFXTextField sliderMaxTextField;
   @FXML
   private JFXTextField sliderMinTextField;
   @FXML
   private JFXSlider slider;
   @FXML
   private Label yoVariableDropLabel;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      
   }
}
