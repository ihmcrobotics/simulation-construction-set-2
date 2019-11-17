package us.ihmc.scs2.definition.terrain;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.scs2.definition.collision.CollisionShapeDefinition;
import us.ihmc.scs2.definition.visual.VisualDefinition;

public class TerrainObjectDefinition
{
   private String name;
   private final List<VisualDefinition> visualDefinitions = new ArrayList<>();
   private final List<CollisionShapeDefinition> collisionShapeDefinitions = new ArrayList<>();

   public TerrainObjectDefinition()
   {
   }
   
   public TerrainObjectDefinition(VisualDefinition visualDefinition, CollisionShapeDefinition collisionShapeDefinition)
   {
      visualDefinitions.add(visualDefinition);
      collisionShapeDefinitions.add(collisionShapeDefinition);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void addVisualDefinition(VisualDefinition visualDefinition)
   {
      visualDefinitions.add(visualDefinition);
   }

   public List<VisualDefinition> getVisualDefinitions()
   {
      return visualDefinitions;
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
