package us.ihmc.scs2.definition.geometry;

import us.ihmc.euclid.tuple2D.Point2D32;
import us.ihmc.euclid.tuple3D.Point3D32;
import us.ihmc.euclid.tuple3D.Vector3D32;

public class TriangleMesh3DDefinition extends GeometryDefinition
{
   private String name;
   private Point3D32[] vertices;
   private Point2D32[] textures;
   private Vector3D32[] normals;
   private int[] triangleIndices;

   public TriangleMesh3DDefinition()
   {
      setName("TriangleMesh");
   }

   public TriangleMesh3DDefinition(Point3D32[] vertices, Point2D32[] textures, Vector3D32[] normals, int[] triangleIndices)
   {
      this();
      setVertices(vertices);
      setTextures(textures);
      setNormals(normals);
      setTriangleIndices(triangleIndices);
   }

   public TriangleMesh3DDefinition(String name, Point3D32[] vertices, Point2D32[] textures, Vector3D32[] normals, int[] triangleIndices)
   {
      setName(name);
      setVertices(vertices);
      setTextures(textures);
      setNormals(normals);
      setTriangleIndices(triangleIndices);
   }

   public TriangleMesh3DDefinition(TriangleMesh3DDefinition other)
   {
      this(other.name, other.vertices, other.textures, other.normals, other.triangleIndices);
   }

   public void setVertices(Point3D32[] vertices)
   {
      this.vertices = vertices;
   }

   public void setTextures(Point2D32[] textures)
   {
      this.textures = textures;
   }

   public void setNormals(Vector3D32[] normals)
   {
      this.normals = normals;
   }

   public void setTriangleIndices(int[] triangleIndices)
   {
      this.triangleIndices = triangleIndices;
   }

   public Point3D32[] getVertices()
   {
      return vertices;
   }

   public Point2D32[] getTextures()
   {
      return textures;
   }

   public Vector3D32[] getNormals()
   {
      return normals;
   }

   public int[] getTriangleIndices()
   {
      return triangleIndices;
   }

   @Override
   public TriangleMesh3DDefinition copy()
   {
      return null;
   }

}
