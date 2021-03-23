package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.layout.Pane;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.YoGraphicFX;

public interface YoGraphicFXCreatorController<G extends YoGraphicFX>
{
   void initialize(SessionVisualizerWindowToolkit toolkit, G yoGraphicToEdit);

   ObservableBooleanValue inputsValidityProperty();

   void resetFields();

   void saveChanges();

   ReadOnlyBooleanProperty hasChangesPendingProperty();

   default boolean hasChangesPending()
   {
      return hasChangesPendingProperty().get();
   }

   G getYoGraphicFX();

   Pane getMainPane();
}
