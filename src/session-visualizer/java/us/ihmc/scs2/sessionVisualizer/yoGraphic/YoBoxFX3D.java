package us.ihmc.scs2.sessionVisualizer.yoGraphic;

import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Affine;
import us.ihmc.javaFXToolkit.JavaFXTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.Orientation3DProperty;
import us.ihmc.scs2.sessionVisualizer.yoComposite.QuaternionProperty;
import us.ihmc.scs2.sessionVisualizer.yoComposite.Tuple3DProperty;

public class YoBoxFX3D extends YoGraphicFX3D
{
   private final Box boxNode = new Box();

   private Tuple3DProperty position = new Tuple3DProperty(null, 0.0, 0.0, 0.0);
   private Orientation3DProperty orientation = new QuaternionProperty(null, 0.0, 0.0, 0.0, 1.0);
   private Tuple3DProperty size = new Tuple3DProperty(null, 0.0, 0.0, 0.0);

   private final Affine affine = new Affine();
   private final PhongMaterial material = new PhongMaterial();

   public YoBoxFX3D()
   {
      boxNode.setMaterial(material);
      boxNode.getTransforms().add(affine);
      boxNode.idProperty().bind(nameProperty());
   }

   @Override
   public void render()
   {
      affine.setToTransform(JavaFXTools.createAffineFromOrientation3DAndTuple(orientation.toQuaternionInWorld(), position.toPoint3DInWorld()));
      if (color == null)
         color = () -> null;
      material.setDiffuseColor(color.get());

      boxNode.setWidth(size.getX());
      boxNode.setHeight(size.getY());
      boxNode.setDepth(size.getZ());
   }

   @Override
   public YoGraphicFX clone()
   {
      YoBoxFX3D clone = new YoBoxFX3D();
      clone.setName(getName());
      clone.setPosition(new Tuple3DProperty(position));
      clone.setOrientation(orientation.clone());
      clone.setSize(new Tuple3DProperty(size));
      clone.setColor(color);
      return clone;
   }

   public void setPosition(Tuple3DProperty position)
   {
      this.position = position;
   }

   public void setOrientation(Orientation3DProperty orientation)
   {
      this.orientation = orientation;
   }

   public void setSize(Tuple3DProperty size)
   {
      this.size = size;
   }

   public Tuple3DProperty getPosition()
   {
      return position;
   }

   public Orientation3DProperty getOrientation()
   {
      return orientation;
   }

   public Tuple3DProperty getSize()
   {
      return size;
   }

   @Override
   public Node getNode()
   {
      return boxNode;
   }
}
