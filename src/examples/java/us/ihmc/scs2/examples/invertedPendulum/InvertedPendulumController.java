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
import us.ihmc.scs2.sharedMemory.interfaces.LinkedYoVariableFactory;
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
   double last_t = 0.0;
   double last_ball_y = 0.0;
   double last_cart_y_desired = 0.0;

   public InvertedPendulumController()
   {
   }

   public void registerYoVariables(LinkedYoVariableFactory rootRegistry)
   {
//      lastTime = rootRegistry.
   }

   public void setupYoVariables(YoVariableRegistry rootRegistry)
   {
      simulationTime = (YoDouble) rootRegistry.getVariable("simulationTime");

      YoVariableRegistry registry = new YoVariableRegistry("sliderController");
      lastTime = new YoDouble("lastTime", registry);
      rootRegistry.addChild(registry);
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

   private void doControl()
   {
      OneDoFJointStateBasics sliderJointState = controllerOutput.getOneDoFJointOutput(sliderJoint);

      double t = simulationTime.getValue();
      double cart_y = sliderJoint.getQ();
      double cart_dy = sliderJoint.getQd();
      double pin_theta = pinJoint.getQ();
      double pin_qtheta = pinJoint.getQd();

      double dt = t - last_t;
      last_t = t;

      double pin_PItoPI = EuclidCoreTools.trimAngleMinusPiToPi(pin_theta);

      if (pin_PItoPI > 0.5 * Math.PI)
      {
         sliderJointState.setEffort(0.0);
         return;
      }

      double estimatedRodLength = 1.0;
      double ball_y = cart_y + estimatedRodLength * Math.sin(pin_PItoPI);

      double ball_dy = (ball_y - last_ball_y) * dt;
      last_ball_y = ball_y;

      double pendulum_kp = 1.0;
      double pendulum_kd = 10.0;

      double cart_y_desired = pendulum_kp * ball_y + pendulum_kd * ball_dy;

      double cart_dy_desired = (cart_y_desired - last_cart_y_desired) * dt;
      last_cart_y_desired = cart_y_desired;

      double cart_error = cart_y_desired - cart_y;
      double cart_derror = cart_dy_desired - cart_dy;

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

   public JointStateReadOnly initialJointState(String jointName)
   {
      OneDoFJointState pinJoint = new OneDoFJointState();
      pinJoint.setConfiguration(0.00001);
      return pinJoint;
   }
}
