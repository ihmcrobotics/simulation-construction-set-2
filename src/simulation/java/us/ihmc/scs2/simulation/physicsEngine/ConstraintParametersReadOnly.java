package us.ihmc.scs2.simulation.physicsEngine;

/**
 * Read-only interface for accessing a set of parameters used for resolving general constraints in
 * {@link ExperimentalPhysicsEngine}.
 * <p>
 * Constraints can either be: robot joint limits or contact between two collidables.
 * </p>
 * 
 * @author Sylvain Bertrand
 */
public interface ConstraintParametersReadOnly
{
   /**
    * Returns the value of the coefficient of restitution.
    * <p>
    * When resolving a constraint with an impulse, the coefficient of restitution defines the relative
    * velocity post-impulse, see
    * <a href="https://en.wikipedia.org/wiki/Coefficient_of_restitution">Wikipedia article</a>.
    * </p>
    * <p>
    * The coefficient of restitution, <i>e</i> below, is recommended to be in [0, 1]:
    * <ul>
    * <li><i>e</i> = 0: The constraint will be perfectly inelastic, the relative velocity post-impulse
    * along the collision axis is 0.
    * <li><i>e</i> = 1: The constraint will be perfectly elastic, the relative velocity post-impulse
    * along the collision axis is equal to the relative pre-impulse negated: the objects interacting
    * perfectly rebound with respect to each other.
    * <li><i>e</i> &in; ]0, 1[: "real-world" inelastic collision, where some of the kinetic energy is
    * dissipated.
    * </ul>
    * </p>
    * 
    * @return the coefficient of restitution.
    */
   double getCoefficientOfRestitution();

   /**
    * Returns the velocity minimum threshold to enable restitution.
    * <p>
    * The threshold on the pre-impulse velocity, if it's magnitude is above the threshold, then the
    * coefficient of restitution is used to resolve the impact, if it is below a coefficient of
    * restitution of zero is used.
    * </p>
    * 
    * @return the restitution threshold on the pre-impulse velocity magnitude.
    */
   double getRestitutionThreshold();

   /**
    * Returns the value of the error reduction parameter.
    * <p>
    * This parameter is inspired on the homonym in the Open Dynamics Engine, see
    * <a href="https://ode.org/ode-latest-userguide.html#sec_3_7"> ODE user guide</a>.
    * </p>
    * <p>
    * The error reduction parameter, or ERP, indicates the percentage of error in the constraint that
    * should be resolved in each simulation tick. The parameter is defined in [0, 1]:
    * <ul>
    * <li>ERP = 0: no correction is applied, if there is constraint error no special effort will be
    * added to resolve it such that it can only stabilize or grow.
    * <li>ERP = 1: a correction is applied to correct the integrity of the constraint error in a single
    * tick. This is expected to provide an unstable simulation.
    * <li>ERP &in; [0, 1]: a correction is applied to correct a percentage of the constraint error in a
    * single tick.
    * </ul>
    * </p>
    * 
    * @return the error reduction parameter.
    */
   double getErrorReductionParameter();
}
