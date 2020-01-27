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
   private YoDouble pinAngularVelocity;
   private YoDouble lastBallY;
   private YoDouble lastCartYDesired;
   private YoDouble cartYDesired;

   public void registerYoVariables(YoVariableRegistry rootRegistry)
   {
      simulationTime = (YoDouble) rootRegistry.getVariable("simulationTime");

      YoVariableRegistry registry = new YoVariableRegistry("InvertedPendulumController");
      lastTime = new YoDouble("lastTime", registry);
      cartY = new YoDouble("cartY", registry);
      cartVelocityY = new YoDouble("cartVelocityY", registry);
      pinAngle = new YoDouble("pinAngle", registry);
      pinAngularVelocity = new YoDouble("pinAngularVelocity", registry);
      lastBallY = new YoDouble("lastBallY", registry);
      lastCartYDesired = new YoDouble("lastCartYDesired", registry);
      cartYDesired = new YoDouble("cartYDesired", registry);
      rootRegistry.addChild(registry);
   }

   private void doControl()
   {
      OneDoFJointStateBasics sliderJointState = controllerOutput.getOneDoFJointOutput(sliderJoint);

      cartY.set(sliderJoint.getQ());
      cartVelocityY.set(sliderJoint.getQd());
      pinAngle.set(pinJoint.getQ());
      pinAngularVelocity.set(pinJoint.getQd());

      double dt = simulationTime.getValue() - lastTime.getValue();
      lastTime.set(simulationTime.getValue());

      pinAngle.set(EuclidCoreTools.trimAngleMinusPiToPi(pinAngle.getValue()));

      if (pinAngle.getValue() > 0.5 * Math.PI)
      {
         sliderJointState.setEffort(0.0);
         return;
      }

      double estimatedRodLength = 1.0;
      double ball_y = cartY.getValue() + estimatedRodLength * Math.sin(pinAngle.getValue());

      double ball_dy = (ball_y - lastBallY.getValue()) * dt;
      lastBallY.set(ball_y);

      double pendulum_kp = 1.0;
      double pendulum_kd = 10.0;

      cartYDesired.set(pendulum_kp * ball_y + pendulum_kd * ball_dy);

      double cart_dy_desired = (cartYDesired.getValue() - lastCartYDesired.getValue()) * dt;
      lastCartYDesired.set(cartYDesired.getValue());

      double cart_error = cartYDesired.getValue() - cartY.getValue();
      double cart_derror = cart_dy_desired - cartVelocityY.getValue();

      double cart_kp = 5.0;
      double cart_kd = 2.0;

      sliderJointState.setEffort(cart_kp * cart_error + cart_kd * cart_derror);

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
