package us.ihmc.scs2.definition.geometry;

import java.util.ArrayList;
import java.util.List;

import us.ihmc.euclid.tuple3D.Vector3D;

public class ModelFileGeometryDefinition implements GeometryDefinition
{
   private String fileName;
   private final List<SubMesh> submeshes = new ArrayList<>();
   private final List<String> resourceDirectories = new ArrayList<>();
   private Vector3D scale;

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public void setScale(Vector3D scale)
   {
      this.scale = scale;
   }

   public String getFileName()
   {
      return fileName;
   }

   public Vector3D getScale()
   {
      return scale;
   }

   public List<SubMesh> getSubmeshes()
   {
      return submeshes;
   }

   public List<String> getResourceDirectories()
   {
      return resourceDirectories;
   }

   public static class SubMesh
   {
      private String name;
      private boolean center;

      public void setName(String name)
      {
         this.name = name;
      }

      public void setCenter(boolean center)
      {
         this.center = center;
      }

      public String getName()
      {
         return name;
      }

      public boolean getCenter()
      {
         return center;
      }
   }
}
