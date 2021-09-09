package us.ihmc.scs2.definition.terrain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

@XmlRootElement(name = "Terrain")
public class TerrainObjectDefinition
{
   private String name;
   private List<VisualDefinition> visualDefinitions = new ArrayList<>();
   private List<CollisionShapeDefinition> collisionShapeDefinitions = new ArrayList<>();

   public TerrainObjectDefinition()
   {
   }

   public TerrainObjectDefinition(VisualDefinition visualDefinition, CollisionShapeDefinition collisionShapeDefinition)
   {
      visualDefinitions.add(visualDefinition);
      collisionShapeDefinitions.add(collisionShapeDefinition);
   }

   @XmlElement
   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   @XmlElement(name = "visual")
   public void setVisualDefinitions(List<VisualDefinition> visualDefinitions)
   {
      this.visualDefinitions = visualDefinitions;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      visualDefinitions.add(visualDefinition);
   }

   public List<VisualDefinition> getVisualDefinitions()
   {
      return visualDefinitions;
   }

   @XmlElement(name = "collision")
   public void setCollisionShapeDefinitions(List<CollisionShapeDefinition> collisionShapeDefinitions)
   {
      this.collisionShapeDefinitions = collisionShapeDefinitions;
   }

   public void addCollisionShapeDefinition(CollisionShapeDefinition collisionShapeDefinition)
   {
      collisionShapeDefinitions.add(collisionShapeDefinition);
   }

   public List<CollisionShapeDefinition> getCollisionShapeDefinitions()
   {
      return collisionShapeDefinitions;
   }
}
