package us.ihmc.scs2.examples.invertedPendulum;

import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.mecano.multiBodySystem.interfaces.JointReadOnly;
import us.ihmc.mecano.multiBodySystem.interfaces.OneDoFJointReadOnly;
import us.ihmc.scs2.definition.controller.ControllerInput;
import us.ihmc.scs2.definition.controller.ControllerOutput;
import us.ihmc.scs2.definition.controller.interfaces.Controller;
import us.ihmc.scs2.definition.controller.interfaces.ControllerDefinition;
import us.ihmc.scs2.definition.state.OneDoFJointState;
import us.ihmc.scs2.definition.state.interfaces.JointStateReadOnly;
import us.ihmc.scs2.definition.state.interfaces.OneDoFJointStateBasics;
import us.ihmc.yoVariables.registry.YoVariableRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public class InvertedPendulumController implements ControllerDefinition
{
   private ControllerInput controllerInput;
   private ControllerOutput controllerOutput;
   private OneDoFJointReadOnly pinJoint;
   private OneDoFJointReadOnly sliderJoint;

   private YoDouble simulationTime;
   private YoDouble lastTime;
   private YoDouble cartY;
   private YoDouble cartVelocityY;
   private YoDouble pinAngle;
   private YoDouble lastBallY;
   private YoDouble lastCartYDesired;
   private YoDouble cartYDesired;
   private YoDouble cartVelocityYDesired;
   private YoDouble simulationDT;
   private YoDouble ballY;
   private YoDouble ballVelocityY;
   private YoDouble cartPositionError;
   private YoDouble cartVelocityError;

   public void registerYoVariables(YoVariableRegistry rootRegistry)
   {
      simulationTime = (YoDouble) rootRegistry.getVariable("simulationTime");

      YoVariableRegistry registry = new YoVariableRegistry("InvertedPendulumController");
      lastTime = new YoDouble("lastTime", registry);
      cartY = new YoDouble("cartY", registry);
      cartVelocityY = new YoDouble("cartVelocityY", registry);
      pinAngle = new YoDouble("pinAngle", registry);
      lastBallY = new YoDouble("lastBallY", registry);
      lastCartYDesired = new YoDouble("lastCartYDesired", registry);
      cartYDesired = new YoDouble("cartYDesired", registry);
      simulationDT = new YoDouble("simulationDT", registry);
      ballY = new YoDouble("ballY", registry);
      ballVelocityY = new YoDouble("ballVelocityY", registry);
      cartVelocityYDesired = new YoDouble("cartVelocityYDesired", registry);
      cartPositionError = new YoDouble("cartPositionError", registry);
      cartVelocityError = new YoDouble("cartVelocityError", registry);
      rootRegistry.addChild(registry);
   }

   private void doControl()
   {
      OneDoFJointStateBasics sliderJointState = controllerOutput.getOneDoFJointOutput(sliderJoint);

      cartY.set(sliderJoint.getQ());
      cartVelocityY.set(sliderJoint.getQd());
      pinAngle.set(pinJoint.getQ());

      simulationDT.set(simulationTime.getValue() - lastTime.getValue());
      lastTime.set(simulationTime.getValue());

      pinAngle.set(EuclidCoreTools.trimAngleMinusPiToPi(pinAngle.getValue()));

      if (simulationDT.getValue() == 0.0 || pinAngle.getValue() > 0.5 * Math.PI)
      {
         sliderJointState.setEffort(0.0);
         return;
      }

      double estimatedRodLength = 1.0;
      ballY.set(cartY.getValue() - estimatedRodLength * Math.sin(pinAngle.getValue()));

      ballVelocityY.set((ballY.getValue() - lastBallY.getValue()) / simulationDT.getValue());
      lastBallY.set(ballY.getValue());

      double pendulum_kp = 1.0;
      double pendulum_kd = 0.0;

      cartYDesired.set(pendulum_kp * ballY.getValue() + pendulum_kd * ballVelocityY.getValue());

      cartVelocityYDesired.set((cartYDesired.getValue() - lastCartYDesired.getValue()) / simulationDT.getValue());
      lastCartYDesired.set(cartYDesired.getValue());

      cartPositionError.set(cartYDesired.getValue() - cartY.getValue());
      cartVelocityError.set(cartVelocityYDesired.getValue() - cartVelocityY.getValue());

      double cart_kp = 5.0;
      double cart_kd = 2.0;

      sliderJointState.setEffort(cart_kp * cartPositionError.getValue() + cart_kd * cartVelocityError.getValue());

      //      long tfloor = (long) Math.floor(t);
      //      if (tfloor % 2 == 0)
      //      {
      //         controllerOutput.getOneDoFJointOutput(pinJoint).setEffort(1.0);
      //         sliderJointState.setEffort(1.0);
      //      }
      //      else
      //      {
      //         controllerOutput.getOneDoFJointOutput(pinJoint).setEffort(-1.0);
      //         controllerOutput.getOneDoFJointOutput(sliderJoint).setEffort(-1.0);
      //      }
   }

   @Override
   public Controller newController(ControllerInput controllerInput, ControllerOutput controllerOutput)
   {
      this.controllerInput = controllerInput;
      this.controllerOutput = controllerOutput;

      for (JointReadOnly allJoint : controllerInput.getInput().getAllJoints())
      {
         if (allJoint.getName().equals("pin"))
         {
            pinJoint = (OneDoFJointReadOnly) allJoint;
         }
         else if (allJoint.getName().equals("slider"))
         {
            sliderJoint = (OneDoFJointReadOnly) allJoint;
         }
      }

      return this::doControl;
   }

   public JointStateReadOnly initialJointState(String jointName)
   {
      OneDoFJointState pinJoint = new OneDoFJointState();
      pinJoint.setConfiguration(0.00001);
      return pinJoint;
   }
}
