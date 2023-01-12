package us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.stage.Window;
import us.ihmc.javaFXToolkit.messager.JavaFXMessager;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.yoSlider.YoButtonDefinition;
import us.ihmc.scs2.definition.yoSlider.YoKnobDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardListDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.SCSGuiConfiguration;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerTopics;
import us.ihmc.scs2.sessionVisualizer.jfx.controllers.sliderboard.bcf2000.YoMultiBCF2000SliderboardWindowController;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.Manager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SecondaryWindowManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.xml.XMLTools;

public class YoSliderboardManager implements Manager
{
   private final Property<YoMultiBCF2000SliderboardWindowController> bcf2000Sliderboard = new SimpleObjectProperty<>(this, "bcf2000Sliderboard", null);
   private final SessionVisualizerToolkit toolkit;
   private final SessionVisualizerTopics topics;
   private final JavaFXMessager messager;

   private YoSliderboardListDefinition initialConfiguration = null;

   public YoSliderboardManager(SessionVisualizerToolkit toolkit)
   {
      messager = toolkit.getMessager();
      topics = toolkit.getTopics();

      this.toolkit = toolkit;
   }

   public void saveSessionConfiguration(SCSGuiConfiguration configuration)
   {
      handleSaveRequest(configuration.getYoSliderboardConfigurationFile());
   }

   @Override
   public void startSession(Session session)
   {
      initialConfiguration = null;

      messager.registerJavaFXSyncedTopicListener(topics.getYoMultiSliderboardSave(), m -> handleSaveRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoMultiSliderboardLoad(), m -> handleLoadRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoMultiSliderboardClearAll(), m -> handleClearRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoMultiSliderboardSet(), m -> handleSetRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardSet(), m -> handleSetRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardRemove(), m -> handleRemoveRequest(m));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardSetButton(), m -> handleSetButtonRequest(m.getKey(), m.getValue()));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardSetKnob(), m -> handleSetKnobRequest(m.getKey(), m.getValue()));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardSetSlider(), m -> handleSetSliderRequest(m.getKey(), m.getValue()));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardClearButton(), m -> handleClearButtonRequest(m.getKey(), m.getValue()));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardClearKnob(), m -> handleClearKnobRequest(m.getKey(), m.getValue()));
      messager.registerJavaFXSyncedTopicListener(topics.getYoSliderboardClearSlider(), m -> handleClearSliderRequest(m.getKey(), m.getValue()));
   }

   private void handleSaveRequest(File file)
   {
      YoSliderboardListDefinition definitionToSave;
      if (bcf2000Sliderboard.getValue() != null)
         definitionToSave = bcf2000Sliderboard.getValue().toYoSliderboardListDefinition();
      else
         definitionToSave = initialConfiguration;

      if (definitionToSave != null)
      {
         try
         {
            XMLTools.saveYoSliderboardListDefinition(new FileOutputStream(file), definitionToSave);
         }
         catch (IOException | JAXBException e)
         {
            e.printStackTrace();
         }
      }
   }

   private void handleLoadRequest(File file)
   {
      LogTools.info("Loading from file: " + file);

      try
      {
         handleSetRequest(XMLTools.loadYoSliderboardListDefinition(new FileInputStream(file)));
      }
      catch (IOException | JAXBException e)
      {
         e.printStackTrace();
      }
   }

   private void handleClearRequest(Boolean m)
   {
      if (bcf2000Sliderboard.getValue() != null)
         bcf2000Sliderboard.getValue().clear();
      else
         initialConfiguration = null;
   }

   private void handleSetRequest(YoSliderboardListDefinition definition)
   {
      if (bcf2000Sliderboard.getValue() != null)
         bcf2000Sliderboard.getValue().setInput(definition);
      else
         initialConfiguration = new YoSliderboardListDefinition(definition);
   }

   private void handleSetRequest(YoSliderboardDefinition definition)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().setSliderboard(definition);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int index = findSliderboardIndex(definition.getName());
         if (index != -1)
            initialConfiguration.getYoSliderboards().get(index).set(definition);
         else
            initialConfiguration.getYoSliderboards().add(definition);
      }
   }

   private void handleRemoveRequest(String sliderboardName)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().closeSliderboard(sliderboardName);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int index = findSliderboardIndex(sliderboardName);
         if (index != -1)
            initialConfiguration.getYoSliderboards().remove(index);
      }
   }

   private void handleSetButtonRequest(String sliderboardName, YoButtonDefinition buttonDefinition)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().setButtonInput(sliderboardName, buttonDefinition);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);
         YoSliderboardDefinition sliderboard;
         if (sliderboardIndex != -1)
         {
            sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);
         }
         else
         {
            sliderboard = new YoSliderboardDefinition(sliderboardName);
            initialConfiguration.getYoSliderboards().add(sliderboard);
         }
         sliderboard.getButtons().add(new YoButtonDefinition(buttonDefinition));
      }
   }

   private void handleSetKnobRequest(String sliderboardName, YoKnobDefinition knobDefinition)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().setKnobInput(sliderboardName, knobDefinition);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);
         YoSliderboardDefinition sliderboard;
         if (sliderboardIndex != -1)
         {
            sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);
         }
         else
         {
            sliderboard = new YoSliderboardDefinition(sliderboardName);
            initialConfiguration.getYoSliderboards().add(sliderboard);
         }
         sliderboard.getKnobs().add(new YoKnobDefinition(knobDefinition));
      }
   }

   private void handleSetSliderRequest(String sliderboardName, YoSliderDefinition sliderDefinition)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().setSliderInput(sliderboardName, sliderDefinition);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);
         YoSliderboardDefinition sliderboard;
         if (sliderboardIndex != -1)
         {
            sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);
         }
         else
         {
            sliderboard = new YoSliderboardDefinition(sliderboardName);
            initialConfiguration.getYoSliderboards().add(sliderboard);
         }
         sliderboard.getSliders().add(new YoSliderDefinition(sliderDefinition));
      }
   }

   private void handleClearButtonRequest(String sliderboardName, int buttonIndex)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().removeButtonInput(sliderboardName, buttonIndex);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);

         if (sliderboardIndex == -1)
            return;

         YoSliderboardDefinition sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);

         List<YoButtonDefinition> buttons = sliderboard.getButtons();
         for (int i = buttons.size() - 1; i >= 0; i--)
         {
            if (i == buttonIndex && buttons.get(i).getIndex() == -1)
               buttons.remove(i);
            else if (buttons.get(i).getIndex() == buttonIndex)
               buttons.remove(i);
         }
      }
   }

   private void handleClearKnobRequest(String sliderboardName, int knobIndex)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().removeKnobInput(sliderboardName, knobIndex);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);

         if (sliderboardIndex == -1)
            return;

         YoSliderboardDefinition sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);

         List<YoKnobDefinition> knobs = sliderboard.getKnobs();
         for (int i = knobs.size() - 1; i >= 0; i--)
         {
            if (i == knobIndex && knobs.get(i).getIndex() == -1)
               knobs.remove(i);
            else if (knobs.get(i).getIndex() == knobIndex)
               knobs.remove(i);
         }
      }
   }

   private void handleClearSliderRequest(String sliderboardName, int sliderIndex)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().removeSliderInput(sliderboardName, sliderIndex);
      }
      else
      {
         if (initialConfiguration == null)
            initialConfiguration = new YoSliderboardListDefinition();

         int sliderboardIndex = findSliderboardIndex(sliderboardName);

         if (sliderboardIndex == -1)
            return;

         YoSliderboardDefinition sliderboard = initialConfiguration.getYoSliderboards().get(sliderboardIndex);

         List<YoSliderDefinition> sliders = sliderboard.getSliders();
         for (int i = sliders.size() - 1; i >= 0; i--)
         {
            if (i == sliderIndex && sliders.get(i).getIndex() == -1)
               sliders.remove(i);
            else if (sliders.get(i).getIndex() == sliderIndex)
               sliders.remove(i);
         }
      }
   }

   private int findSliderboardIndex(String sliderboardName)
   {
      if (sliderboardName == null)
         return -1;

      List<YoSliderboardDefinition> yoSliderboards = initialConfiguration.getYoSliderboards();

      for (int i = 0; i < yoSliderboards.size(); i++)
      {
         YoSliderboardDefinition sliderboard = yoSliderboards.get(i);
         if (sliderboardName.equals(sliderboard.getName()))
            return i;
      }
      return -1;
   }

   @Override
   public void stopSession()
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().close();
         bcf2000Sliderboard.setValue(null);
      }
   }

   @Override
   public boolean isSessionLoaded()
   {
      // TODO Should probably return something else here.
      return false;
   }

   public void openBCF2000SliderboardWindow(Window requestSource)
   {
      if (bcf2000Sliderboard.getValue() != null)
      {
         bcf2000Sliderboard.getValue().showWindow();
         return;
      }

      try
      {
         FXMLLoader fxmlLoader = new FXMLLoader(SessionVisualizerIOTools.YO_MULTI_SLIDERBOARD_BCF2000_WINDOW_URL);
         fxmlLoader.load();
         YoMultiBCF2000SliderboardWindowController controller = fxmlLoader.getController();
         controller.initialize(toolkit);
         if (initialConfiguration != null)
         {
            controller.setInput(initialConfiguration);
            initialConfiguration = null;
         }
         bcf2000Sliderboard.setValue(controller);
         SecondaryWindowManager.initializeSecondaryWindowWithOwner(requestSource, controller.getWindow());
         controller.showWindow();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
