package us.ihmc.scs2.sessionVisualizer;

import org.fxyz3d.scene.Skybox;

import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;

public class SCS2Skybox
{
   private final Skybox skybox;

   public SCS2Skybox(SubScene subScene)
   {
      this(100000.0, subScene);
   }

   public SCS2Skybox(double size, SubScene subScene)
   {
      PerspectiveCamera camera = (PerspectiveCamera) subScene.getCamera();
      skybox = loadCloudyCrown(size, camera);
      skybox.getTransforms().add(new Rotate(-90.0, Rotate.X_AXIS));
      skybox.setMouseTransparent(true);
   }

   public static Skybox loadCloudyCrown(double size, PerspectiveCamera camera)
   {
      return loadSixImageSkybox("cloudy", "png", size, camera);
   }

   public static Skybox loadCartoonLandscape(double size, PerspectiveCamera camera)
   {
      Image image = new Image(SessionVisualizerIOTools.getSkyboxResource("skybox-cartoon.png"));
      return new Skybox(image, size, camera);
   }

   private static Skybox loadSixImageSkybox(String directoryPath, String fileExtension, double size, PerspectiveCamera camera)
   {
      Image topImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Up." + fileExtension));
      Image bottomImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Down." + fileExtension));
      Image leftImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Left." + fileExtension));
      Image rightImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Right." + fileExtension));
      Image frontImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Front." + fileExtension));
      Image backImg = new Image(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Back." + fileExtension));
      return new Skybox(topImg, bottomImg, leftImg, rightImg, frontImg, backImg, size, camera);
   }

   public Node getSkybox()
   {
      return skybox;
   }
}
