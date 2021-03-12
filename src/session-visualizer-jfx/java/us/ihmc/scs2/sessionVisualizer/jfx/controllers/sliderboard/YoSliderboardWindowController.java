package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import us.ihmc.scs2.sessionVisualizer.sliderboard.BCF2000SliderboardController.Slider;

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
   private BCF2000SliderboardController sliderboard;

   public void initialize(SessionVisualizerToolkit toolkit, Window owner)
   {
      yoSliderControllers = Arrays.asList(yoSlider0Controller,
                                          yoSlider1Controller,
                                          yoSlider2Controller,
                                          yoSlider3Controller,
                                          yoSlider4Controller,
                                          yoSlider5Controller,
                                          yoSlider6Controller,
                                          yoSlider7Controller);

      sliderboard = BCF2000SliderboardController.searchAndConnectToDevice();

      for (int i = 0; i < yoSliderControllers.size(); i++)
      {
         YoSliderController yoSliderController = yoSliderControllers.get(i);
         yoSliderController.initialize(toolkit, sliderboard.getSlider(Slider.values()[i]));
      }

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
      yoSlider0Controller.setInput(yoSliderboard.getSlider1());
      yoSlider1Controller.setInput(yoSliderboard.getSlider2());
      yoSlider2Controller.setInput(yoSliderboard.getSlider3());
      yoSlider3Controller.setInput(yoSliderboard.getSlider4());
      yoSlider4Controller.setInput(yoSliderboard.getSlider5());
      yoSlider5Controller.setInput(yoSliderboard.getSlider6());
      yoSlider6Controller.setInput(yoSliderboard.getSlider7());
      yoSlider7Controller.setInput(yoSliderboard.getSlider8());
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
      for (YoSliderController yoSliderController : yoSliderControllers)
      {
         yoSliderController.close();
      }

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
      definition.setSlider1(yoSlider0Controller.toYoSliderDefinition());
      definition.setSlider2(yoSlider1Controller.toYoSliderDefinition());
      definition.setSlider3(yoSlider2Controller.toYoSliderDefinition());
      definition.setSlider4(yoSlider3Controller.toYoSliderDefinition());
      definition.setSlider5(yoSlider4Controller.toYoSliderDefinition());
      definition.setSlider6(yoSlider5Controller.toYoSliderDefinition());
      definition.setSlider7(yoSlider6Controller.toYoSliderDefinition());
      definition.setSlider8(yoSlider7Controller.toYoSliderDefinition());
      return new YoSliderboardListDefinition(null, Collections.singletonList(definition));
   }
}
