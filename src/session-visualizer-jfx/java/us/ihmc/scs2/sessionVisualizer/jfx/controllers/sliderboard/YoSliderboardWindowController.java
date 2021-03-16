package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

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
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardListDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Button;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Knob;
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Slider;
import us.ihmc.scs2.sessionVisualizer.sliderboard.SliderboardVariable;

public class YoSliderboardWindowController
{
   @FXML
   private AnchorPane mainAnchorPane;
   @FXML
   private YoKnobController yoKnob0Controller, yoKnob1Controller, yoKnob2Controller, yoKnob3Controller, yoKnob4Controller, yoKnob5Controller, yoKnob6Controller,
         yoKnob7Controller;
   @FXML
   private YoButtonController yoButton0Controller, yoButton1Controller, yoButton2Controller, yoButton3Controller, yoButton4Controller, yoButton5Controller,
         yoButton6Controller, yoButton7Controller;
   @FXML
   private YoButtonController yoButton8Controller, yoButton9Controller, yoButton10Controller, yoButton11Controller, yoButton12Controller, yoButton13Controller,
         yoButton14Controller, yoButton15Controller;
   @FXML
   private YoSliderController yoSlider0Controller, yoSlider1Controller, yoSlider2Controller, yoSlider3Controller, yoSlider4Controller, yoSlider5Controller,
         yoSlider6Controller, yoSlider7Controller;

   private List<YoKnobController> yoKnobControllers;
   private List<YoButtonController> yoButtonControllers;
   private List<YoSliderController> yoSliderControllers;

   private Stage window;
   private BCF2000SliderboardController sliderboard;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      yoKnobControllers = Arrays.asList(yoKnob0Controller,
                                        yoKnob1Controller,
                                        yoKnob2Controller,
                                        yoKnob3Controller,
                                        yoKnob4Controller,
                                        yoKnob5Controller,
                                        yoKnob6Controller,
                                        yoKnob7Controller);
      yoButtonControllers = Arrays.asList(yoButton0Controller,
                                          yoButton1Controller,
                                          yoButton2Controller,
                                          yoButton3Controller,
                                          yoButton4Controller,
                                          yoButton5Controller,
                                          yoButton6Controller,
                                          yoButton7Controller,
                                          yoButton8Controller,
                                          yoButton9Controller,
                                          yoButton10Controller,
                                          yoButton11Controller,
                                          yoButton12Controller,
                                          yoButton13Controller,
                                          yoButton14Controller,
                                          yoButton15Controller);
      yoSliderControllers = Arrays.asList(yoSlider0Controller,
                                          yoSlider1Controller,
                                          yoSlider2Controller,
                                          yoSlider3Controller,
                                          yoSlider4Controller,
                                          yoSlider5Controller,
                                          yoSlider6Controller,
                                          yoSlider7Controller);

      sliderboard = BCF2000SliderboardController.searchAndConnectToDevice();

      if (sliderboard == null)
         LogTools.error("Could not connect to BCF2000 sliderboard");

      for (int i = 0; i < yoKnobControllers.size(); i++)
      {
         YoKnobController yoKnobController = yoKnobControllers.get(i);
         SliderboardVariable knob = sliderboard == null ? null : sliderboard.getKnob(Knob.values()[i]);
         yoKnobController.initialize(toolkit, knob);
      }

      for (int i = 0; i < yoButtonControllers.size(); i++)
      {
         YoButtonController yoButtonController = yoButtonControllers.get(i);
         SliderboardVariable button = sliderboard == null ? null : sliderboard.getButton(Button.values()[i]);
         yoButtonController.initialize(toolkit, button);
      }

      for (int i = 0; i < yoSliderControllers.size(); i++)
      {
         YoSliderController yoSliderController = yoSliderControllers.get(i);
         SliderboardVariable slider = sliderboard == null ? null : sliderboard.getSlider(Slider.values()[i]);
         yoSliderController.initialize(toolkit, slider);
      }

      if (sliderboard != null)
         sliderboard.start();

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

      JavaFXMessager messager = toolkit.getMessager();
      SessionVisualizerTopics topics = toolkit.getTopics();

      File configurationFile = messager.createInput(topics.getYoSliderboardLoadConfiguration()).get();
      if (configurationFile != null)
         load(configurationFile);

      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardLoadConfiguration(), this::load);
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardSaveConfiguration(), this::save);
      toolkit.setYoSliderboardWindowController(this);
   }

   public void load(File file)
   {
      LogTools.info("Loading from file: " + file);

      try
      {
         YoSliderboardListDefinition definition = XMLTools.loadYoSliderboardListDefinition(new FileInputStream(file));
         setInput(definition);
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void save(File file)
   {
      LogTools.info("Saving to file: " + file);

      try
      {
         XMLTools.saveYoSliderboardListDefinition(new FileOutputStream(file), toYoSliderboardListDefinition());
      }
      catch (FileNotFoundException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   public void setInput(YoSliderboardListDefinition input)
   {
      List<YoSliderboardDefinition> yoSliderboards = input.getYoSliderboards();

      if (yoSliderboards == null || yoSliderboards.isEmpty())
         return;

      YoSliderboardDefinition yoSliderboard = yoSliderboards.get(0);
      if (yoSliderboard.getKnobs() != null)
      {
         for (int i = 0; i < Math.min(yoKnobControllers.size(), yoSliderboard.getKnobs().size()); i++)
         {
            yoKnobControllers.get(i).setInput(yoSliderboard.getKnobs().get(i));
         }
      }
      if (yoSliderboard.getButtons() != null)
      {
         for (int i = 0; i < Math.min(yoButtonControllers.size(), yoSliderboard.getButtons().size()); i++)
         {
            yoButtonControllers.get(i).setInput(yoSliderboard.getButtons().get(i));
         }
      }
      if (yoSliderboard.getSliders() != null)
      {
         for (int i = 0; i < Math.min(yoSliderControllers.size(), yoSliderboard.getSliders().size()); i++)
         {
            yoSliderControllers.get(i).setInput(yoSliderboard.getSliders().get(i));
         }
      }
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

   public void close()
   {
      for (YoButtonController yoButtonController : yoButtonControllers)
         yoButtonController.close();
      for (YoKnobController yoKnobController : yoKnobControllers)
         yoKnobController.close();
      for (YoSliderController yoSliderController : yoSliderControllers)
         yoSliderController.close();

      if (sliderboard != null)
         sliderboard.close();
      window.close();
   }

   public Stage getWindow()
   {
      return window;
   }

   public YoSliderboardListDefinition toYoSliderboardListDefinition()
   {
      YoSliderboardDefinition definition = new YoSliderboardDefinition();
      definition.setKnobs(yoKnobControllers.stream().map(YoKnobController::toYoKnobDefinition).collect(Collectors.toList()));
      definition.setButtons(yoButtonControllers.stream().map(YoButtonController::toYoButtonDefinition).collect(Collectors.toList()));
      definition.setSliders(yoSliderControllers.stream().map(YoSliderController::toYoSliderDefinition).collect(Collectors.toList()));
      return new YoSliderboardListDefinition(null, Collections.singletonList(definition));
   }
}
