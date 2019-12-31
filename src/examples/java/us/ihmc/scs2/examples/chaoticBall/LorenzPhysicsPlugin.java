package us.ihmc.scs2.examples.chaoticBall;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import us.ihmc.commons.time.Stopwatch;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;
import us.ihmc.log.LogTools;
import us.ihmc.mecano.multiBodySystem.interfaces.MultiBodySystemBasics;
import us.ihmc.mecano.yoVariables.multiBodySystem.YoSixDoFJoint;
import us.ihmc.scs2.simulation.physicsEngine.RobotPhysicsEnginePlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class LorenzPhysicsPlugin implements RobotPhysicsEnginePlugin
{
   private MultiBodySystemBasics system;

   @Override
   public void setMultiBodySystem(MultiBodySystemBasics multiBodySystem)
   {
      system = multiBodySystem;
   }

   double t = 0.0;

   private Point3D q = new Point3D();

   @Override
   public void doScience(double dt, Vector3DReadOnly gravity)
   {
      YoSixDoFJoint joint = (YoSixDoFJoint) system.getJointsToConsider().get(0);

      double x = joint.getJointPose().getX();
      double y = joint.getJointPose().getY();
      double z = joint.getJointPose().getZ();

      double o = 10;
      double B = 8.0 / 3.0;
      double p = 28;

      double xd = o * (y - x);
      double yd = x * (p - z) - y;
      double zd = x * y - B * z;

      q.setX(x + xd * dt);
      q.setY(y + yd * dt);
      q.setZ(z + zd * dt);
      joint.setJointPosition(q);

      t += dt;
   }

   public static void main(String[] args)
   {
      Stopwatch stopwatch = new Stopwatch().start();

      int iterations = 0;
      double sum = 0.0;
      double approximation = Double.NaN;

      Random random = new Random(System.nanoTime());

      ArrayList<Pair<Integer, Integer>> cards = new ArrayList();
      for (int suit = 0; suit < 4; suit++)
      {
         for (int number = 0; number < 13; number++)
         {
            cards.add(Pair.of(suit,  number));
         }
      }
      ArrayList<Pair<Integer, Integer>> deck = new ArrayList();
      ArrayList<Pair<Integer, Integer>> hand = new ArrayList();

      HashMap<Integer, MutableInt> numberMap = new HashMap();
      for (int i = 0; i < 13; i++)
      {
         numberMap.put(i, new MutableInt());
      }
      ArrayList<MutableInt> numberSets = new ArrayList<>();


      System.out.println("goal: 0.00144057623049219...");
      while (true)
      {
         deck.addAll(cards);

         // draw cards
         for (int i = 0; i < 5; i++)
         {
            Pair<Integer, Integer> drawnCard = deck.remove(Math.abs(random.nextInt() % deck.size()));
            if (numberMap.containsKey(drawnCard.getRight()))
            {
               numberMap.get(drawnCard.getRight()).increment();
            }
            else
            {
               numberMap.put(drawnCard.getRight(), new MutableInt(1));
            }
         }

         boolean isFullHouse = false;
         if (numberMap.size() == 2)
         {
            numberSets.addAll(numberMap.values());

            if (numberSets.get(0).getValue() == 2 && numberSets.get(1).getValue() == 3)
            {
               isFullHouse = true;
            }
            if (numberSets.get(0).getValue() == 3 && numberSets.get(1).getValue() == 2)
            {
               isFullHouse = true;
            }
         }

         double result = isFullHouse ? 1.0 : 0.0;

         sum += result;
         ++iterations;
         approximation = sum / iterations;

         if (stopwatch.lapElapsed() > 1.0)
         {
            // print info
            System.out.println("iterations: " + iterations + " approximation: " + approximation);

            stopwatch.lap();
         }

         deck.clear();
         hand.clear();
         numberMap.clear();
         numberSets.clear();
      }
   }
}