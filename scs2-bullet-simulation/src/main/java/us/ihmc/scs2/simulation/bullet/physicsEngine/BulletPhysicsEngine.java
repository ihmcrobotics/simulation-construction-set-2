package us.ihmc.scs2.simulation.bullet.physicsEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.tools.JointStateType;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.RobotStateDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.YoTimer;
import us.ihmc.scs2.simulation.physicsEngine.PhysicsEngine;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.scs2.simulation.robot.RobotExtension;
import us.ihmc.scs2.simulation.robot.RobotInterface;
import us.ihmc.yoVariables.euclid.YoPoint3D;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;

/**
 * Documentation:
 * https://web.archive.org/web/20170706235814/http://www.bulletphysics.org/mediawiki-1.5.8/index.php/Main_Page
 * https://web.archive.org/web/20170602122143/http://www.bulletphysics.org/mediawiki-1.5.8/index.php/Collision_Callbacks_and_Triggers
 * https://pybullet.org/Bullet/phpBB3/viewtopic.php?t=2568
 * https://github.com/kripken/bullet/blob/master/Demos/CollisionInterfaceDemo/CollisionInterfaceDemo.cpp
 * https://libgdx.com/wiki/extensions/physics/bullet/bullet-physics
 * PyBullet Quickstart Guide:
 * https://docs.google.com/document/d/10sXEhzFRSnvFcl3XxNGhnD4N2SedqwdAvK3dsihxVUA/edit#heading=h.i4yo7k7s2gvx
 *
 * CF_CUSTOM_MATERIAL_CALLBACK has to be added to get a callback when contacts are initially made
 */
public class BulletPhysicsEngine implements PhysicsEngine
{
   static
   {
      Bullet.init();
      LogTools.info("Loaded Bullet version {}", LinearMath.btGetVersion());
   }
   private btCollisionConfiguration collisionConfiguration;
   private btCollisionDispatcher collisionDispatcher;
   private btBroadphaseInterface broadphase;
   private btMultiBodyConstraintSolver solver;
   private btMultiBodyDynamicsWorld multiBodyDynamicsWorld;
   private final ArrayList<btRigidBody> rigidBodies = new ArrayList<>();
   private final ArrayList<btMultiBody> multiBodies = new ArrayList<>();
   private final ArrayList<btCollisionObject> collisionObjects = new ArrayList<>();
   private final ArrayList<BulletTerrainObject> terrainObjects = new ArrayList<>();

   private final ReferenceFrame inertialFrame;
   private final YoRegistry rootRegistry;
   private final YoRegistry physicsEngineRegistry = new YoRegistry(getClass().getSimpleName());
   private final YoDouble runTickExpectedTimeRate = new YoDouble("runTickExpectedTimeRate", physicsEngineRegistry);
   private final YoTimer runBulletPhysicsEngineSimulateTimer = new YoTimer("runBulletPhysicsEngineSimulateTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer runBulletStepSimulateTimer = new YoTimer("runBulletStepSimulateTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer runControllerManagerTimer = new YoTimer("runControllerManagerTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer runCopyDataFromBulletToSCSTimer = new YoTimer("runCopyDataFromBulletToSCSTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer runCopyDataFromSCSToBulletTimer = new YoTimer("runCopyDataFromSCSToBulletTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoTimer runUpdateFramesSensorsTimer = new YoTimer("runUpdateFramesSensorsTimer", TimeUnit.MILLISECONDS, physicsEngineRegistry);
   private final YoDouble bulletPhysicsEngineRealTimeRate = new YoDouble("bulletPhysicsEngineRealTimeRate", physicsEngineRegistry);
   
   private final ArrayList<YoPoint3D> contactPoints = new ArrayList<>();
   {
      for (int i = 0; i < 100; i++)
      {
         contactPoints.add(new YoPoint3D("contactPoint" + i, physicsEngineRegistry));
      }
   }
   
   private final List<BulletRobot> robotList = new ArrayList<>();
   private final List<TerrainObjectDefinition> terrainObjectDefinitions = new ArrayList<>();

   private boolean initialize = true;
   private final ArrayList<Runnable> postTickCallbacks = new ArrayList<>();

   public BulletPhysicsEngine(ReferenceFrame inertialFrame, YoRegistry rootRegistry)
   {
      this.inertialFrame = inertialFrame;
      this.rootRegistry = rootRegistry;
      
      collisionConfiguration = new btDefaultCollisionConfiguration();
      collisionDispatcher = new btCollisionDispatcher(collisionConfiguration);
      broadphase = new btDbvtBroadphase();
      solver = new btMultiBodyConstraintSolver();
      multiBodyDynamicsWorld = new btMultiBodyDynamicsWorld(collisionDispatcher, broadphase, solver, collisionConfiguration);
      Vector3 gravity = new Vector3(0.0f, 0.0f, -9.81f);
      multiBodyDynamicsWorld.setGravity(gravity);

//      BulletTools.setupPostTickCallback(multiBodyDynamicsWorld, postTickCallbacks);
   }

   private void setupDrawingDebugMeshes()
   {

   }

   @Override
   public boolean initialize(Vector3DReadOnly gravity)
   {
      if (!initialize)
         return false;

      rootRegistry.addChild(physicsEngineRegistry);

      for (BulletRobot robot : robotList)
      {
         robot.initializeState();
         robot.updateSensors();
         robot.getControllerManager().initializeControllers();
      }
      initialize = false;
      return true;
   }

   @Override
   public void simulate(double currentTime, double dt, Vector3DReadOnly gravity)
   {
      runTickExpectedTimeRate.set(dt * 1000);
      if (initialize(gravity))
         return;

      runBulletPhysicsEngineSimulateTimer.start();
      
      runControllerManagerTimer.start();
      for (BulletRobot robot : robotList)
      {
         robot.getControllerManager().updateControllers(currentTime);
         robot.getControllerManager().writeControllerOutput(JointStateType.EFFORT);
         robot.getControllerManager().writeControllerOutputForJointsToIgnore(JointStateType.values());
      }
      runControllerManagerTimer.stop();
      
      runCopyDataFromSCSToBulletTimer.start();
      for (BulletRobot robot : robotList)
      {
         robot.saveRobotBeforePhysicsState();
      }
      runCopyDataFromSCSToBulletTimer.stop();
      
      int maxSubSteps = 1; // With SCS, we want every tick to get a buffer entry and step through things one step at a time.
      float fixedTimeStep = (float) dt;  // SCS has a fixed timestep already so let's just use it
      float timePassedSinceThisWasCalledLast = fixedTimeStep; // We are essentially disabling interpolation here
      runBulletStepSimulateTimer.start();
      multiBodyDynamicsWorld.stepSimulation(timePassedSinceThisWasCalledLast, maxSubSteps, fixedTimeStep);
      runBulletStepSimulateTimer.stop();

      runCopyDataFromBulletToSCSTimer.start();
      for (BulletRobot robot : robotList)
      {
         robot.updateFromBulletData(this, dt);
      }
      runCopyDataFromBulletToSCSTimer.stop();

      runUpdateFramesSensorsTimer.start();
      for (BulletRobot robot : robotList)
      {
         robot.updateFrames(); 
         robot.updateSensors();
      }
      runUpdateFramesSensorsTimer.stop();
      runBulletPhysicsEngineSimulateTimer.stop();
      bulletPhysicsEngineRealTimeRate.set(runTickExpectedTimeRate.getValue() / runBulletPhysicsEngineSimulateTimer.getTimer().getValue());
   }

   @Override
   public void pause()
   {
      for (BulletRobot robot : robotList)
      {
         robot.getControllerManager().pauseControllers();
      }   
   }

   @Override
   public void addRobot(Robot robot)
   {
      inertialFrame.checkReferenceFrameMatch(robot.getInertialFrame());
      BulletRobot bulletRobot = new BulletRobot(robot, physicsEngineRegistry, this);
      multiBodyDynamicsWorld.addMultiBody(bulletRobot.getBulletMultiBody());
      rootRegistry.addChild(bulletRobot.getRegistry());
      robotList.add(bulletRobot);
   }

   public void destroy()
   {
      System.out.println("Destroy");
      for (btRigidBody rigidBody : rigidBodies)
      {
         multiBodyDynamicsWorld.removeRigidBody(rigidBody);
         rigidBody.dispose();
      }
      
      for (btMultiBody multiBody : multiBodies)
      {
         multiBodyDynamicsWorld.removeMultiBody(multiBody);
         multiBody.dispose();
      }

      for (btCollisionObject shape : collisionObjects)
      {
         multiBodyDynamicsWorld.removeCollisionObject(shape);
         shape.dispose();
      }
      collisionObjects.clear();
      
      multiBodyDynamicsWorld.dispose();
      collisionConfiguration.dispose();
      collisionDispatcher.dispose();
      broadphase.dispose();
      solver.dispose();
   }

   @Override
   public void addTerrainObject(TerrainObjectDefinition terrainObjectDefinition)
   {
      terrainObjectDefinitions.add(terrainObjectDefinition);
      terrainObjects.add(new BulletTerrainObject(terrainObjectDefinition, multiBodyDynamicsWorld));
   }

   public void addMultiBodyCollisionShape(btMultiBodyLinkCollider collisionShape, int collisionGroup, int collisionGroupMask)
   {
      multiBodyDynamicsWorld.addCollisionObject(collisionShape, collisionGroup, collisionGroupMask);
    }

   public void addPostTickCallback(Runnable postTickCallback)
   {
      postTickCallbacks.add(postTickCallback);
   }
   
   @Override
   public ReferenceFrame getInertialFrame()
   {
      return inertialFrame;
   }

   @Override
   public List<? extends Robot> getRobots()
   {
      return robotList.stream().map(BulletRobot::getRobot).collect(Collectors.toList());
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotList.stream().map(RobotInterface::getRobotDefinition).collect(Collectors.toList());
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return terrainObjectDefinitions;
   }

   @Override
   public List<RobotStateDefinition> getBeforePhysicsRobotStateDefinitions()
   {
      return robotList.stream().map(RobotExtension::getRobotBeforePhysicsStateDefinition).collect(Collectors.toList());
   }

   @Override
   public YoRegistry getPhysicsEngineRegistry()
   {
      return physicsEngineRegistry;
   }

   public btMultiBodyDynamicsWorld getBulletMultiBodyDynamicsWorld()
   {
      return multiBodyDynamicsWorld;
   }
}
