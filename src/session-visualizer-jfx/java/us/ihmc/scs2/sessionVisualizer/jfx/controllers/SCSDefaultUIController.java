package us.ihmc.scs2.sessionVisualizer.jfx.controllers;

import javafx.scene.layout.Pane;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerWindowToolkit;

public interface SCSDefaultUIController
{
   void initialize(SessionVisualizerWindowToolkit toolkit);

   Pane getMainPane();
}
