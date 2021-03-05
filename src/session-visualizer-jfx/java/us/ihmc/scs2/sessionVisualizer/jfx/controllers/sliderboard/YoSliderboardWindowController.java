package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.util.Arrays;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.sliderboard.old.MidiSliderBoard;

public class YoSliderboardWindowController
{
   @FXML
   private AnchorPane mainAnchorPane;
   @FXML
   private YoSliderController yoSlider0Controller;
   @FXML
   private YoSliderController yoSlider1Controller;
   @FXML
   private YoSliderController yoSlider2Controller;
   @FXML
   private YoSliderController yoSlider3Controller;
   @FXML
   private YoSliderController yoSlider4Controller;
   @FXML
   private YoSliderController yoSlider5Controller;
   @FXML
   private YoSliderController yoSlider6Controller;
   @FXML
   private YoSliderController yoSlider7Controller;

   private List<YoSliderController> yoSliderControllers;

   private Stage window;
   private Window owner;
   private MidiSliderBoard midiSliderBoard;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      this.owner = owner;

      yoSliderControllers = Arrays.asList(yoSlider0Controller,
                                          yoSlider1Controller,
                                          yoSlider2Controller,
                                          yoSlider3Controller,
                                          yoSlider4Controller,
                                          yoSlider5Controller,
                                          yoSlider6Controller,
                                          yoSlider7Controller);

      midiSliderBoard = new MidiSliderBoard();

      for (YoSliderController yoSliderController : yoSliderControllers)
      {
         yoSliderController.initialize(toolkit, midiSliderBoard);
      }

      window = new Stage(StageStyle.UTILITY);
      window.addEventHandler(KeyEvent.KEY_PRESSED, e ->
      {
         if (e.getCode() == KeyCode.ESCAPE)
            window.close();
      });

      owner.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> window.close());
      window.setTitle("YoSliderboard controller");
      window.setScene(new Scene(mainAnchorPane));
      window.initOwner(owner);
   }

   public void showWindow()
   {
      window.setOpacity(0.0);
      window.toFront();
      window.show();
      Timeline timeline = new Timeline();
      KeyFrame key = new KeyFrame(Duration.seconds(0.125), new KeyValue(window.opacityProperty(), 1.0));
      timeline.getKeyFrames().add(key);
      timeline.play();
   }

   public Stage getWindow()
   {
      return window;
   }
}
