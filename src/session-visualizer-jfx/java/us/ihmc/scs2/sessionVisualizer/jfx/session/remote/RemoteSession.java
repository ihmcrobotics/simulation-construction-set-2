package us.ihmc.scs2.sessionVisualizer.jfx.session.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import us.ihmc.commons.Conversions;
import us.ihmc.robotDataLogger.YoVariableClientInterface;
import us.ihmc.robotDataLogger.handshake.LogHandshake;
import us.ihmc.robotDataLogger.handshake.YoVariableHandshakeParser;
import us.ihmc.robotDataLogger.util.DebugRegistry;
import us.ihmc.robotDataLogger.websocket.command.DataServerCommand;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.SessionProperties;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.RobotModelLoader;
import us.ihmc.scs2.sessionVisualizer.jfx.yoGraphic.SCS1GraphicConversionTools;

public class RemoteSession extends Session
{
   private static final double MAX_DELAY_MILLI = 200.0;

   private YoVariableClientInterface yoVariableClientInterface;

   private final String sessionName;
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final List<YoGraphicDefinition> yoGraphicDefinitions;
   private final Runnable robotStateUpdater;

   private final AtomicLong serverTimestamp = new AtomicLong(-1);
   private final AtomicLong latestDataTimestamp = new AtomicLong(-1);
   private final LoggerStatusUpdater loggerStatusUpdater = new LoggerStatusUpdater();

   private int bufferRecordTickPeriod = 1;
   private boolean initializeServerUpdateRate = true;

   public RemoteSession(YoVariableClientInterface yoVariableClientInterface,
                        LogHandshake handshake,
                        YoVariableHandshakeParser handshakeParser,
                        DebugRegistry debugRegistry)
   {
      this.yoVariableClientInterface = yoVariableClientInterface;

      sessionName = yoVariableClientInterface.getServerName();

      rootRegistry.addChild(handshakeParser.getRootRegistry());
      rootRegistry.addChild(debugRegistry.getYoRegistry());
      yoGraphicDefinitions = SCS1GraphicConversionTools.toYoGraphicDefinitions(handshakeParser.getYoGraphicsListRegistry());

      RobotDefinition robotDefinition = RobotModelLoader.loadModel(handshake.getModelName(),
                                                                   handshake.getResourceDirectories(),
                                                                   handshake.getModel(),
                                                                   handshake.getResourceZip());
      robotStateUpdater = RobotModelLoader.setupRobotUpdater(robotDefinition, handshakeParser, rootRegistry);
      if (robotDefinition != null)
         robotDefinitions.add(robotDefinition);

      setSessionDTSeconds(handshakeParser.getDt());
      setSessionModeTask(SessionMode.RUNNING, () ->
      {
         if (!this.yoVariableClientInterface.isConnected())
            setSessionMode(SessionMode.PAUSE);

         if (stopCurrentSessionTask.get())
            activeScheduledFuture.cancel(false);
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

   @Override
   protected long computePlaybackTaskPeriod()
   {
      /*
       * We let the yoVariableClient handle the bufferRecordTickPeriod feature, so we're not setting
       * updating the corresponding field in Session. Thus, it cannot compute the playback DT properly
       * without temporarily setting the Session.bufferRecordTickPeriod.
       */
      int superBufferRecordTickPeriod = super.getBufferRecordTickPeriod();
      super.submitBufferRecordTickPeriod(bufferRecordTickPeriod);
      long playbackTaskPeriod = super.computePlaybackTaskPeriod();
      super.submitBufferRecordTickPeriod(superBufferRecordTickPeriod);
      return playbackTaskPeriod;
   }

   @Override
   public SessionProperties getSessionProperties()
   {
      return new SessionProperties(getActiveMode(), getRunAtRealTimeRate(), getPlaybackRealTimeRate(), getSessionDTNanoseconds(), bufferRecordTickPeriod);
   }

   public void receivedTimestampOnly(long timestamp)
   {
      serverTimestamp.set(timestamp);
   }

   public void receivedTimestampAndData(long timestamp)
   {
      if (!hasSessionStarted() || getActiveMode() != SessionMode.RUNNING)
         return;

      if (initializeServerUpdateRate)
      {
         updateServerUpdateRate();
         initializeServerUpdateRate = false;
         return;
      }

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
         if (yoVariableClientInterface.reconnect())
            updateServerUpdateRate();
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

      initializeServerUpdateRate = true;
      yoVariableClientInterface.disconnect();
   }

   @Override
   protected double doSpecificRunTick()
   {
      if (robotStateUpdater != null)
         robotStateUpdater.run();
      return Conversions.nanosecondsToSeconds(latestDataTimestamp.get());
   }

   @Override
   protected void finalizeRunTick()
   {
      if (Conversions.nanosecondsToMilliseconds(getDelay()) < MAX_DELAY_MILLI * bufferRecordTickPeriod)
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

   @Override
   public void submitBufferRecordTickPeriod(int bufferRecordTickPeriod)
   {
      if (bufferRecordTickPeriod == this.bufferRecordTickPeriod)
         return;
      this.bufferRecordTickPeriod = Math.max(1, bufferRecordTickPeriod);
      updateServerUpdateRate();
   }

   private void updateServerUpdateRate()
   {
      int updateRateInMilliseconds = (int) TimeUnit.NANOSECONDS.toMillis(bufferRecordTickPeriod * getSessionDTNanoseconds());
      yoVariableClientInterface.setVariableUpdateRate(updateRateInMilliseconds);
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

   @Override
   public List<YoGraphicDefinition> getYoGraphicDefinitions()
   {
      return yoGraphicDefinitions;
   }

   public LoggerStatusUpdater getLoggerStatusUpdater()
   {
      return loggerStatusUpdater;
   }
}
