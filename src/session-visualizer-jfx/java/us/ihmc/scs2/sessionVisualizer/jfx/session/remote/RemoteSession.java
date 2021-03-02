package us.ihmc.scs2.sessionVisualizer.jfx.session.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import us.ihmc.commons.Conversions;
import us.ihmc.robotDataLogger.YoVariableClientInterface;
import us.ihmc.robotDataLogger.handshake.LogHandshake;
import us.ihmc.robotDataLogger.handshake.YoVariableHandshakeParser;
import us.ihmc.robotDataLogger.util.DebugRegistry;
import us.ihmc.robotDataLogger.websocket.command.DataServerCommand;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.RobotModelLoader;

public class RemoteSession extends Session
{
   private static final double MAX_DELAY_MILLI = 200.0;

   private YoVariableClientInterface yoVariableClientInterface;

   private final String sessionName;
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final Runnable robotStateUpdater;

   private final AtomicLong serverTimestamp = new AtomicLong(-1);
   private final AtomicLong latestDataTimestamp = new AtomicLong(-1);
   private final LoggerStatusUpdater loggerStatusUpdater = new LoggerStatusUpdater();

   public RemoteSession(YoVariableClientInterface yoVariableClientInterface, LogHandshake handshake, YoVariableHandshakeParser handshakeParser,
                        DebugRegistry debugRegistry)
   {
      this.yoVariableClientInterface = yoVariableClientInterface;

      sessionName = yoVariableClientInterface.getServerName();

      rootRegistry.addChild(handshakeParser.getRootRegistry());
      rootRegistry.addChild(debugRegistry.getYoRegistry());

      RobotDefinition robotDefinition = RobotModelLoader.loadModel(handshake.getModelName(),
                                                                   handshake.getResourceDirectories(),
                                                                   handshake.getModel(),
                                                                   handshake.getResourceZip());
      robotStateUpdater = RobotModelLoader.setupRobotUpdater(robotDefinition, handshakeParser, rootRegistry);
      if (robotDefinition != null)
         robotDefinitions.add(robotDefinition);

      setSessionTickToTimeIncrement(Conversions.secondsToNanoseconds(handshakeParser.getDt()));
      setSessionModeTask(SessionMode.RUNNING, () ->
      {
         /* Do nothing, the client thread calls runTick(). */
      });
      addSessionPropertiesListener(properties ->
      {
         if (properties.getActiveMode() == SessionMode.RUNNING)
            reconnect();
         else
            disconnect();
      });
      submitDesiredBufferPublishPeriod(Conversions.secondsToNanoseconds(1.0 / 60.0));
      setSessionMode(SessionMode.RUNNING);
   }

   public long getDelay()
   {
      return serverTimestamp.get() - latestDataTimestamp.get();
   }

   @Override
   protected long computeRunTaskPeriod()
   {
      return Conversions.secondsToNanoseconds(0.01);
   }

   public void receivedTimestampOnly(long timestamp)
   {
      serverTimestamp.set(timestamp);
   }

   public void receivedTimestampAndData(long timestamp)
   {
      if (!hasSessionStarted() || getActiveMode() != SessionMode.RUNNING)
         return;
      latestDataTimestamp.set(timestamp);
      runTick();
   }

   private void reconnect()
   {
      if (yoVariableClientInterface.isConnected())
         return;

      sharedBuffer.setInPoint(sharedBuffer.getProperties().getCurrentIndex());

      try
      {
         yoVariableClientInterface.reconnect();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void disconnect()
   {
      if (!yoVariableClientInterface.isConnected())
         return;

      yoVariableClientInterface.disconnect();
   }

   @Override
   protected void doSpecificRunTick()
   {
      if (robotStateUpdater != null)
         robotStateUpdater.run();
   }

   @Override
   protected void finalizeRunTick()
   {
      if (Conversions.nanosecondsToMilliseconds(getDelay()) < MAX_DELAY_MILLI)
      {
         super.finalizeRunTick();
      }
      else
      {
         sharedBuffer.writeBuffer();
         processBufferRequests(false);
         publishBufferProperties(sharedBuffer.getProperties());
      }
   }

   public void receivedCommand(DataServerCommand command, int argument)
   {
      loggerStatusUpdater.updateStatus(command, argument);
   }

   public void close()
   {
      if (yoVariableClientInterface != null)
      {
         if (yoVariableClientInterface.isConnected())
            yoVariableClientInterface.disconnect();
         yoVariableClientInterface.stop();
      }
   }

   @Override
   public String getSessionName()
   {
      return sessionName;
   }

   @Override
   public List<RobotDefinition> getRobotDefinitions()
   {
      return robotDefinitions;
   }

   @Override
   public List<TerrainObjectDefinition> getTerrainObjectDefinitions()
   {
      return Collections.emptyList();
   }

   public LoggerStatusUpdater getLoggerStatusUpdater()
   {
      return loggerStatusUpdater;
   }
}
