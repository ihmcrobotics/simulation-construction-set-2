package us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic;

import javafx.scene.Node;
import javafx.scene.shape.Line;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple2D.Vector2D;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.Tuple2DProperty;

public class YoLineFX2D extends YoGraphicFX2D
{
   private Tuple2DProperty origin = new Tuple2DProperty(null, 0.0, 0.0);
   private Tuple2DProperty direction = new Tuple2DProperty(null, 0.0, 1.0);
   private Tuple2DProperty destination = null;

   private final Line lineNode = new Line();

   public YoLineFX2D()
   {
      lineNode.idProperty().bind(nameProperty());
   }

   @Override
   public void render()
   {
      if (fillColor == null)
         fillColor = () -> null;
      if (strokeColor == null)
         strokeColor = () -> null;
      if (strokeWidth == null)
         strokeWidth = DEFAULT_STROKE_WIDTH;
      lineNode.setFill(fillColor.get());
      lineNode.setStroke(strokeColor.get());

      double scale = lineNode.getLocalToSceneTransform().deltaTransform(1.0, 0.0).getX();
      lineNode.setStrokeWidth(strokeWidth.get() / scale);

      if (origin == null || (direction == null && destination == null))
      {
         lineNode.setStartX(Double.NaN);
         return;
      }

      Point2D originInWorld = origin.toPoint2DInWorld();
      lineNode.setStartX(originInWorld.getX());
      lineNode.setStartY(originInWorld.getY());

      if (direction != null)
      {
         Vector2D directionInWorld = direction.toVector2DInWorld();
         lineNode.setEndX(originInWorld.getX() + directionInWorld.getX());
         lineNode.setEndY(originInWorld.getY() + directionInWorld.getY());
      }
      else
      {
         Point2D destinationInWorld = destination.toPoint2DInWorld();
         lineNode.setEndX(destinationInWorld.getX());
         lineNode.setEndY(destinationInWorld.getY());
      }
   }

   public void setOrigin(Tuple2DProperty origin)
   {
      this.origin = origin;
   }

   public void setDirection(Tuple2DProperty direction)
   {
      this.direction = direction;
   }

   public void setDestination(Tuple2DProperty destination)
   {
      this.destination = destination;
   }

   @Override
   public YoGraphicFX clone()
   {
      YoLineFX2D clone = new YoLineFX2D();
      clone.setName(getName());
      clone.setOrigin(new Tuple2DProperty(origin));
      clone.setDirection(direction != null ? new Tuple2DProperty(direction) : null);
      clone.setDestination(destination != null ? new Tuple2DProperty(destination) : null);
      clone.setFillColor(fillColor);
      clone.setStrokeColor(strokeColor);
      clone.setStrokeWidth(strokeWidth);
      return clone;
   }

   public Tuple2DProperty getOrigin()
   {
      return origin;
   }

   public Tuple2DProperty getDirection()
   {
      return direction;
   }

   public Tuple2DProperty getDestination()
   {
      return destination;
   }

   @Override
   public Node getNode()
   {
      return lineNode;
   }
}
