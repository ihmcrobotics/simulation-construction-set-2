/**
 * Skybox.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package us.ihmc.scs2.sessionVisualizer.jfx;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.javafx.tk.PlatformImage;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class Skybox extends Group
{
   private final Translate translate = new Translate();
   private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 100000.0);

   private final Affine backAffine = new Affine();
   private final Affine frontAffine = new Affine();
   private final Affine topAffine = new Affine();
   private final Affine bottomAffine = new Affine();
   private final Affine leftAffine = new Affine();
   private final Affine rightAffine = new Affine();

   private final ImageView topView = new ImageView();
   private final ImageView bottomView = new ImageView();
   private final ImageView leftView = new ImageView();
   private final ImageView rightView = new ImageView();
   private final ImageView backView = new ImageView();
   private final ImageView frontView = new ImageView();
   private final ImageView[] views = new ImageView[] {topView, leftView, backView, rightView, frontView, bottomView};

   private Camera camera;
   private ChangeListener<Transform> cameraMotionListener = (observable, oldValue, newValue) ->
   {
      translate.setX(newValue.getTx());
      translate.setY(newValue.getTy());
      translate.setZ(newValue.getTz());
   };;

   public Skybox()
   {
      for (ImageView view : views)
      {
         view.setSmooth(true);
         view.setPreserveRatio(true);
         view.fitWidthProperty().bind(size);
         view.fitHeightProperty().bind(size);
      }

      updateTransforms(size.get());

      backView.getTransforms().addAll(backAffine);
      frontView.getTransforms().addAll(frontAffine);
      topView.getTransforms().addAll(topAffine);
      bottomView.getTransforms().addAll(bottomAffine);
      leftView.getTransforms().addAll(leftAffine);
      rightView.getTransforms().addAll(rightAffine);

      getTransforms().add(translate);
      getChildren().addAll(views);
      setMouseTransparent(true);
   }

   private void updateTransforms(double size)
   {
      backAffine.setToIdentity();
      backAffine.appendTranslation(-0.5 * size, -0.5 * size, 0.5 * size);
      backAffine.appendRotation(+90.0, Point3D.ZERO, Rotate.Z_AXIS);
      backAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.X_AXIS);

      frontAffine.setToIdentity();
      frontAffine.appendTranslation(0.5 * size, 0.5 * size, 0.5 * size);
      frontAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.Z_AXIS);
      frontAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.X_AXIS);

      topAffine.setToIdentity();
      topAffine.appendTranslation(-0.5 * size, 0.5 * size, 0.5 * size);
      topAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.Z_AXIS);

      bottomAffine.setToIdentity();
      bottomAffine.appendTranslation(0.5 * size, 0.5 * size, -0.5 * size);
      bottomAffine.appendRotation(180.0, Point3D.ZERO, Rotate.X_AXIS);
      bottomAffine.appendRotation(+90.0, Point3D.ZERO, Rotate.Z_AXIS);

      leftAffine.setToIdentity();
      leftAffine.appendTranslation(-0.5 * size, 0.5 * size, 0.5 * size);
      leftAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.X_AXIS);

      rightAffine.setToIdentity();
      rightAffine.appendTranslation(0.5 * size, -0.5 * size, 0.5 * size);
      rightAffine.appendRotation(180.0, Point3D.ZERO, Rotate.Z_AXIS);
      rightAffine.appendRotation(-90.0, Point3D.ZERO, Rotate.X_AXIS);
   }

   public void setupSkybox(Image topImage, Image bottomImage, Image leftImage, Image rightImage, Image frontImage, Image backImage)
   {
      backView.setImage(backImage);
      frontView.setImage(frontImage);
      topView.setImage(topImage);
      bottomView.setImage(bottomImage);
      leftView.setImage(leftImage);
      rightView.setImage(rightImage);
   }

   public void setupSkybox(String directoryPath, String fileExtension)
   {
      Image topImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Up." + fileExtension));
      Image bottomImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Down." + fileExtension));
      Image leftImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Left." + fileExtension));
      Image rightImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Right." + fileExtension));
      Image frontImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Front." + fileExtension));
      Image backImage = loadImage(SessionVisualizerIOTools.getSkyboxResource(directoryPath + "/Back." + fileExtension));
      setupSkybox(topImage, bottomImage, leftImage, rightImage, frontImage, backImage);
   }

   private static Image loadImage(InputStream is)
   {
      Image image = new Image(is);
      try
      {
         is.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return image;
   }

   public void setupCloudyCrown()
   {
      setupSkybox("cloudy", "png");
   }

   public void setupCamera(Camera camera)
   {
      if (this.camera != null)
         this.camera.localToSceneTransformProperty().removeListener(cameraMotionListener);
      this.camera = camera;
      if (camera != null)
         camera.localToSceneTransformProperty().addListener(cameraMotionListener);
   }

   public void setSize(double size)
   {
      this.size.set(size);
   }

   public double getSize()
   {
      return size.get();
   }

   public DoubleProperty sizeProperty()
   {
      return size;
   }

   public void dispose()
   {
      setupCamera(null);

      try
      {
         Method method = Image.class.getDeclaredMethod("setPlatformImage", PlatformImage.class);
         method.setAccessible(true);
         for (ImageView view : views)
         {
            if (view.getImage() != null)
               method.invoke(view.getImage(), (PlatformImage) null);
         }
      }
      catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
      {
         e.printStackTrace();
      }

      for (ImageView view : views)
      {

         view.setImage(null);
      }
   }
}
