package us.ihmc.scs2.examples.sessionVisualizer.jfx;

import static us.ihmc.scs2.sessionVisualizer.jfx.definition.JavaFXTriangleMesh3DDefinitionInterpreter.interpretDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.fxyz3d.shapes.primitives.Text3DMesh;
import org.fxyz3d.shapes.primitives.TexturedMesh;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import us.ihmc.euclid.axisAngle.AxisAngle;
import us.ihmc.euclid.geometry.ConvexPolygon2D;
import us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.shape.convexPolytope.tools.EuclidPolytopeFactories;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple2D.interfaces.Point2DReadOnly;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.javaFXToolkit.JavaFXTools;
import us.ihmc.javaFXToolkit.cameraControllers.FocusBasedCameraMouseEventHandler;
import us.ihmc.javaFXToolkit.scenes.View3DFactory;
import us.ihmc.javaFXToolkit.starter.ApplicationRunner;
import us.ihmc.javaFXToolkit.text.Text3D;
import us.ihmc.scs2.sessionVisualizer.TriangleMesh3DFactories;

public class TriangleMesh3DFactoriesVisualizer
{
   private static final boolean USE_TEXTURE = false;

   public TriangleMesh3DFactoriesVisualizer(Stage primaryStage)
   {
      primaryStage.setTitle(getClass().getSimpleName());

      View3DFactory view3dFactory = new View3DFactory(600, 400);
      FocusBasedCameraMouseEventHandler cameraController = view3dFactory.addCameraController(true);
      cameraController.changeCameraPosition(-1.0, -1.0, 1.0);
      view3dFactory.addWorldCoordinateSystem(0.3);
      view3dFactory.addNodeToView(createAxisLabels());

      PhongMaterial defaultMaterial;
      if (USE_TEXTURE)
      {
         defaultMaterial = new PhongMaterial();
         Image image = new Image(getClass().getClassLoader().getResourceAsStream("debuggingTextureGrid.jpg"));
         defaultMaterial.setDiffuseMap(image);
      }
      else
      {
         defaultMaterial = new PhongMaterial(Color.CYAN);
      }

      MeshView arcTorus = new MeshView(interpretDefinition(TriangleMesh3DFactories.ArcTorus(0.0, 1.0 * Math.PI, 0.15, 0.05, 64)));
      arcTorus.setMaterial(defaultMaterial);
      arcTorus.setTranslateX(0.0);
      arcTorus.setTranslateY(0.0);
      view3dFactory.addNodeToView(arcTorus);

      MeshView cylinder = new MeshView(interpretDefinition(TriangleMesh3DFactories.Cylinder(0.1, 0.3, 64, true)));
      cylinder.setMaterial(defaultMaterial);
      cylinder.setTranslateX(0.0);
      cylinder.setTranslateY(0.5);
      view3dFactory.addNodeToView(cylinder);

      MeshView flatRectangle = new MeshView(interpretDefinition(TriangleMesh3DFactories.FlatRectangle(0.2, 0.3, 0.0)));
      flatRectangle.setMaterial(defaultMaterial);
      flatRectangle.setTranslateX(0.0);
      flatRectangle.setTranslateY(1.0);
      view3dFactory.addNodeToView(flatRectangle);

      MeshView cone = new MeshView(interpretDefinition(TriangleMesh3DFactories.Cone(0.3, 0.1, 64)));
      cone.setMaterial(defaultMaterial);
      cone.setTranslateX(0.0);
      cone.setTranslateY(-0.5);
      view3dFactory.addNodeToView(cone);

      MeshView wedge = new MeshView(interpretDefinition(TriangleMesh3DFactories.Ramp(0.3, 0.2, 0.2)));
      wedge.setMaterial(defaultMaterial);
      wedge.setTranslateX(0.0);
      wedge.setTranslateY(-1.0);
      view3dFactory.addNodeToView(wedge);

      MeshView sphere = new MeshView(interpretDefinition(TriangleMesh3DFactories.Ellipsoid(0.15, 0.05, 0.2, 64, 64)));
      sphere.setMaterial(defaultMaterial);
      sphere.setTranslateX(0.5);
      sphere.setTranslateY(0.0);
      view3dFactory.addNodeToView(sphere);

      List<? extends Point2DReadOnly> polygonVertices = createPolygon();
      MeshView extrudedPolygon = new MeshView(interpretDefinition(TriangleMesh3DFactories.ExtrudedPolygon(polygonVertices, 0.1)));
      extrudedPolygon.setMaterial(defaultMaterial);
      extrudedPolygon.setTranslateX(0.5);
      extrudedPolygon.setTranslateY(0.5);
      view3dFactory.addNodeToView(extrudedPolygon);

      MeshView line = new MeshView(interpretDefinition(TriangleMesh3DFactories.Line(0.0, 0.0, 0.2, 0.0, 0.5, 0.0, 0.01)));
      line.setMaterial(defaultMaterial);
      line.setTranslateX(0.5);
      line.setTranslateY(1.0);
      view3dFactory.addNodeToView(line);

      MeshView pyramidCube = new MeshView(interpretDefinition(TriangleMesh3DFactories.PyramidBox(0.1, 0.2, 0.1, 0.25)));
      pyramidCube.setMaterial(defaultMaterial);
      pyramidCube.setTranslateX(0.5);
      pyramidCube.setTranslateY(-0.5);
      view3dFactory.addNodeToView(pyramidCube);

      MeshView capsule = new MeshView(interpretDefinition(TriangleMesh3DFactories.Capsule(0.2, 0.025, 0.05, 0.07, 16, 16)));
      capsule.setMaterial(defaultMaterial);
      capsule.setTranslateX(0.5);
      capsule.setTranslateY(-1.0);
      view3dFactory.addNodeToView(capsule);

      MeshView genTruncatedCone = new MeshView(interpretDefinition(TriangleMesh3DFactories.TruncatedCone(0.25, 0.2, 0.07, 0.05, 0.1, 64, false)));
      genTruncatedCone.setMaterial(defaultMaterial);
      genTruncatedCone.setTranslateX(-0.5);
      genTruncatedCone.setTranslateY(0.0);
      view3dFactory.addNodeToView(genTruncatedCone);

      MeshView hemiEllipsoid = new MeshView(interpretDefinition(TriangleMesh3DFactories.HemiEllipsoid(0.10, 0.20, 0.2, 64, 64)));
      hemiEllipsoid.setMaterial(defaultMaterial);
      hemiEllipsoid.setTranslateX(-0.5);
      hemiEllipsoid.setTranslateY(0.5);
      view3dFactory.addNodeToView(hemiEllipsoid);

      MeshView tetrahedron = new MeshView(interpretDefinition(TriangleMesh3DFactories.Tetrahedron(0.3)));
      tetrahedron.setMaterial(defaultMaterial);
      tetrahedron.setTranslateX(-0.5);
      tetrahedron.setTranslateY(1.0);
      view3dFactory.addNodeToView(tetrahedron);

      MeshView polygon = new MeshView(interpretDefinition(TriangleMesh3DFactories.PolygonCounterClockwise(null, polygonVertices)));
      polygon.setMaterial(defaultMaterial);
      polygon.setTranslateX(-0.5);
      polygon.setTranslateY(-0.5);
      view3dFactory.addNodeToView(polygon);

      MeshView polytope = new MeshView(interpretDefinition(TriangleMesh3DFactories.ConvexPolytope(EuclidPolytopeFactories.newIcosahedron(0.2))));
      polytope.setMaterial(defaultMaterial);
      polytope.setTranslateX(-0.5);
      polytope.setTranslateY(-1.0);
      view3dFactory.addNodeToView(polytope);

      MeshView box = new MeshView(interpretDefinition(TriangleMesh3DFactories.Box(0.1, 0.2, 0.3, true)));
      box.setMaterial(defaultMaterial);
      box.setTranslateX(1.0);
      box.setTranslateY(0.0);
      view3dFactory.addNodeToView(box);

      MeshView polygon3D = new MeshView(interpretDefinition(TriangleMesh3DFactories.PolygonCounterClockwise(polygonVertices.stream().map(p2D ->
      {
         Point3D p3D = new Point3D(p2D);
         p3D.setZ(0.1 * (Math.random() - 0.5));
         return p3D;
      }).collect(Collectors.toList()))));
      polygon3D.setMaterial(defaultMaterial);
      polygon3D.setTranslateX(1.0);
      polygon3D.setTranslateY(0.5);
      view3dFactory.addNodeToView(polygon3D);

      MeshView torus = new MeshView(interpretDefinition(TriangleMesh3DFactories.Torus(0.15, 0.05, 64)));
      torus.setMaterial(defaultMaterial);
      torus.setTranslateX(1.0);
      torus.setTranslateY(-0.5);
      view3dFactory.addNodeToView(torus);

      MeshView stpBox = new MeshView(interpretDefinition(TriangleMesh3DFactories.toSTPBox3DMesh(null, 0.3, 0.4, 0.5, 0.05, 5.0, true)));
      stpBox.setMaterial(defaultMaterial);
      stpBox.setTranslateX(1.5);
      stpBox.setTranslateY(-0.5);
      view3dFactory.addNodeToView(stpBox);

      MeshView stpCapsule = new MeshView(interpretDefinition(TriangleMesh3DFactories.toSTPCapsule3DMesh(null, 0.4, 0.8, 0.025, 5.0, true)));
      stpCapsule.setMaterial(defaultMaterial);
      stpCapsule.setTranslateX(1.5);
      stpCapsule.setTranslateY(0.0);
      view3dFactory.addNodeToView(stpCapsule);

      primaryStage.setMaximized(true);
      primaryStage.setScene(view3dFactory.getScene());
      primaryStage.show();
   }

   private List<? extends Point2DReadOnly> createPolygon()
   {
      ConvexPolygon2D polygon = EuclidGeometryRandomTools.nextConvexPolygon2D(new Random(), 0.3, 10);
      List<Point2D> vertices = polygon.getPolygonVerticesView().stream().map(Point2D::new).collect(Collectors.toList());
      Collections.reverse(vertices);
      return vertices;
   }

   private static Node createAxisLabels()
   {
      AxisAngle xAxisAngle = new AxisAngle();
      xAxisAngle.appendYawRotation(-0.25 * Math.PI);
      xAxisAngle.appendRollRotation(-0.5 * Math.PI);
      Node xLabel = createLabel("x", Color.RED, 0.10, 0.001, new Point3D(300, 0, 25), xAxisAngle);

      AxisAngle yAxisAngle = new AxisAngle();
      yAxisAngle.appendYawRotation(-0.25 * Math.PI);
      yAxisAngle.appendRollRotation(-0.5 * Math.PI);
      Node yLabel = createLabel("y", Color.GREEN, 0.10, 0.001, new Point3D(0, 300, 25), yAxisAngle);

      AxisAngle zAxisAngle = new AxisAngle();
      zAxisAngle.appendYawRotation(-0.25 * Math.PI);
      zAxisAngle.appendRollRotation(-0.5 * Math.PI);
      Node zLabel = createLabel("z", Color.BLUE, 0.10, 0.001, new Point3D(0, 0, 325), zAxisAngle);
      return new Group(xLabel, yLabel, zLabel);
   }

   private static Node createLabel(String text, Color color, double height, double thickness, Point3DReadOnly position, Orientation3DReadOnly orientation)
   {
      Text3D label = new Text3D(text);
      label.setFontHeight(height);
      label.setFontThickness(thickness);
      ((Text3DMesh) label.getNode()).getChildren().forEach(child -> ((TexturedMesh) child).setMaterial(new PhongMaterial(color)));
      label.getNode().getTransforms().add(JavaFXTools.createAffineFromOrientation3DAndTuple(orientation, position));

      return label.getNode();
   }

   public static void main(String[] args)
   {
      ApplicationRunner.runApplication(TriangleMesh3DFactoriesVisualizer::new);
   }
}