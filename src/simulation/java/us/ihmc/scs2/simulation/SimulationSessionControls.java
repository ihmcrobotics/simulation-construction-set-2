package us.ihmc.scs2.simulation;

import java.util.function.Consumer;

public interface SimulationSessionControls
{
   void pause();

   void simulate();

   void simulate(double duration);

   void simulate(int numberOfTicks);
   
   boolean simulateAndWait(double duration);
   
   boolean simulateAndWait(int numberOfTicks);

   void addSimulationThrowableListener(Consumer<Throwable> listener);
}
