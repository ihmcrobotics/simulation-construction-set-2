package us.ihmc.scs2.sessionVisualizer.controllers.menu;

import org.apache.commons.lang3.mutable.MutableBoolean;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.scs2.sessionVisualizer.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sharedMemory.CropBufferRequest;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;

public class DataBufferMenuController
{
   @FXML
   private TextField bufferSizeTextField;
   @FXML
   private MenuItem sizeMenuItem;

   private JavaFXMessager messager;
   private SessionVisualizerTopics topics;

   private boolean initializeBufferSizeTextField = true;
   private Property<YoBufferPropertiesReadOnly> bufferProperties;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
      this.messager = toolkit.getMessager();
      topics = toolkit.getTopics();
      bufferProperties = messager.createPropertyInput(topics.getYoBufferCurrentProperties(), null);

      TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter());
      formatter.setValue(0);
      bufferSizeTextField.setTextFormatter(formatter);
      /*
       * TODO: Workaround for a bug in JFX that's causing the previous MenuItem to be triggered and
       * pressing enter while editing the TextField. Registering an EventHandler (even empty) using
       * TextField.setOnAction(...) changes the internal logic and prevents the bug from occurring, see:
       * @formatter:off
       * https://stackoverflow.com/questions/51307577/javafx-custommenuitem-strange-behaviour-with-textfield
       * @formatter:on
       */
      bufferSizeTextField.setOnAction(e ->
      {
      });

      MutableBoolean updatingBuffer = new MutableBoolean(false);
      MutableBoolean updatingFormatter = new MutableBoolean(false);

      bufferProperties.addListener((o, oldValue, newValue) ->
      {
         if (!initializeBufferSizeTextField && (oldValue == null || newValue.getSize() == oldValue.getSize()))
            return;

         if (updatingBuffer.isFalse())
         {
            updatingFormatter.setTrue();
            formatter.setValue(newValue.getSize());
            initializeBufferSizeTextField = false;
            updatingFormatter.setFalse();
         }
      });

      formatter.valueProperty().addListener((o, oldValue, newValue) ->
      {
         if (updatingBuffer.isFalse())
         {
            updatingFormatter.setTrue();
            messager.submitMessage(topics.getYoBufferCurrentSizeRequest(), newValue);
            updatingFormatter.setFalse();
         }
      });
   }

   @FXML
   private void requestCropDataBuffer()
   {
      if (bufferProperties.getValue() != null)
      {
         CropBufferRequest cropBufferRequest = CropBufferRequest.toCropBufferRequest(bufferProperties.getValue().getInPoint(),
                                                                                     bufferProperties.getValue().getOutPoint());
         messager.submitMessage(topics.getYoBufferCropRequest(), cropBufferRequest);
      }
   }
}
