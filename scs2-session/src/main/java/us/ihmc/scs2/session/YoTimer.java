package us.ihmc.scs2.session;

import java.util.concurrent.TimeUnit;

import us.ihmc.yoVariables.exceptions.IllegalOperationException;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoTimer
{
   private final YoDouble timer;
   private final double scaleFromNanoseconds;

   private long start = -1;
   private long lastUpdateTime = -1;

   public YoTimer(String namePrefix, TimeUnit timerUnit, YoRegistry registry)
   {
      timer = new YoDouble(namePrefix + "[" + timeUnitSuffix(timerUnit) + "]", registry);
      scaleFromNanoseconds = scaleFromNanoseconds(timerUnit);
   }

   private static String timeUnitSuffix(TimeUnit timeUnit)
   {
      switch (timeUnit)
      {
         case DAYS:
            return "day";
         case HOURS:
            return "hour";
         case MINUTES:
            return "min";
         case SECONDS:
            return "sec";
         case MILLISECONDS:
            return "ms";
         case MICROSECONDS:
            return "us";
         case NANOSECONDS:
            return "ns";
         default:
            throw new IllegalArgumentException("Unexpected value: " + timeUnit);
      }
   }

   private static double scaleFromNanoseconds(TimeUnit timeUnit)
   {
      switch (timeUnit)
      {
         case DAYS:
            return 1.0 / 86400.0 * 1.0e-9;
         case HOURS:
            return 1.0 / 3600.0 * 1.0e-9;
         case MINUTES:
            return 1.0 / 60.0 * 1.0e-9;
         case SECONDS:
            return 1.0e-9;
         case MILLISECONDS:
            return 1.0e-6;
         case MICROSECONDS:
            return 1.0e-3;
         case NANOSECONDS:
            return 1.0;
         default:
            throw new IllegalArgumentException("Unexpected value: " + timeUnit);
      }
   }

   public void start()
   {
      start = System.nanoTime();
   }

   public void start(long now, TimeUnit timeUnit)
   {
      start(timeUnit.toNanos(now));
   }

   public void start(long nowNanosec)
   {
      start = nowNanosec;
   }

   public void stop()
   {
      stop(System.nanoTime());
   }

   public void stop(long now, TimeUnit timeUnit)
   {
      stop(timeUnit.toNanos(now));
   }

   public void stop(long nowNanosec)
   {
      if (start == -1)
         throw new IllegalOperationException("Call start before each call of stop.");

      timer.set((nowNanosec - start) * scaleFromNanoseconds);
      start = -1;
   }

   public void reset()
   {
      start = -1;
      lastUpdateTime = -1;
   }

   public void update()
   {
      update(System.nanoTime());
   }

   public void update(long now, TimeUnit timeUnit)
   {
      update(timeUnit.toNanos(now));
   }

   public void update(long nowNanosec)
   {
      if (lastUpdateTime > -1)
      {
         timer.set((double) (nowNanosec - lastUpdateTime) * scaleFromNanoseconds);
      }

      lastUpdateTime = nowNanosec;
   }

   public YoDouble getTimer()
   {
      return timer;
   }
}