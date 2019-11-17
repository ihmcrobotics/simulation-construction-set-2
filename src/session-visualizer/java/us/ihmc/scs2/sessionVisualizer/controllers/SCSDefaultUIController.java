package us.ihmc.scs2.sessionVisualizer.controllers;

import javafx.scene.layout.Pane;
import javafx.stage.Window;
import us.ihmc.scs2.sessionVisualizer.managers.SessionVisualizerToolkit;

public interface SCSDefaultUIController
{
   void initialize(SessionVisualizerToolkit toolkit, Window owner);

   Pane getMainPane();
}
