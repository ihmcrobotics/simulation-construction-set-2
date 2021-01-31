package us.ihmc.scs2.sessionVisualizer.jfx.controllers;

import javafx.scene.layout.Pane;
import javafx.stage.Window;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;

public interface SCSDefaultUIController
{
   void initialize(SessionVisualizerToolkit toolkit, Window owner);

   Pane getMainPane();
}
