package us.ihmc.scs2.sessionVisualizer.jfx.managers;

import java.util.List;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import us.ihmc.javaFXToolkit.shapes.JavaFXCoordinateSystem;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.jfx.SCS2Skybox;
import us.ihmc.scs2.sessionVisualizer.jfx.definition.JavaFXVisualTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;

public class EnvironmentManager implements Manager
{
   private final Group rootNode = new Group();
   private final Group terrainObjectGraphics = new Group();

   private final BackgroundExecutorManager backgroundExecutorManager;

   public EnvironmentManager(BackgroundExecutorManager backgroundExecutorManager)
   {
      this.backgroundExecutorManager = backgroundExecutorManager;
      rootNode.getChildren().add(terrainObjectGraphics);
   }

   public void addWorldCoordinateSystem(double size)
   {
      backgroundExecutorManager.executeInBackground(() ->
      {
         Node node = new JavaFXCoordinateSystem(size);
         JavaFXMissingTools.runLater(getClass(), () -> rootNode.getChildren().add(node));
      });
   }

   public void addSkybox(SubScene subScene)
   {
      backgroundExecutorManager.executeInBackground(() ->
      {
         Node skybox = new SCS2Skybox(subScene).getSkybox();
         JavaFXMissingTools.runLater(getClass(), () -> rootNode.getChildren().add(skybox));
      });
   }

   @Override
   public void startSession(Session session)
   {
      List<TerrainObjectDefinition> terrainObjectDefinitions = session.getTerrainObjectDefinitions();
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () ->
      {
         for (TerrainObjectDefinition definition : terrainObjectDefinitions)
            terrainObjectGraphics.getChildren().add(JavaFXVisualTools.collectNodes(definition.getVisualDefinitions()));
      });
   }

   @Override
   public void stopSession()
   {
      JavaFXMissingTools.runLaterIfNeeded(getClass(), () -> terrainObjectGraphics.getChildren().clear());
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }

   public Group getRootNode()
   {
      return rootNode;
   }
}
