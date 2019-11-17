package us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.yoGraphic.YoGraphicFX;

public interface YoGraphicFXCreatorController<G extends YoGraphicFX>
{
   void initialize(SessionVisualizerToolkit toolkit, G yoGraphicToEdit, Window owner);

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
