package us.ihmc.scs2.sessionVisualizer.session.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import us.ihmc.commons.Conversions;
import us.ihmc.log.LogTools;
import us.ihmc.robotDataLogger.handshake.YoVariableHandshakeParser;
import us.ihmc.robotDataLogger.logger.LogPropertiesReader;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.sessionVisualizer.tools.RobotDataLogTools;
import us.ihmc.scs2.sessionVisualizer.tools.RobotModelLoader;

public class LogSession extends Session
{
   private final String sessionName;
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final Runnable robotStateUpdater;

   private final File logDirectory;
   private final LogDataReader logDataReader;
   private final LogPropertiesReader logProperties;

   private final AtomicInteger logPositionRequest = new AtomicInteger(-1);

   public LogSession(File logDirectory, ProgressConsumer progressConsumer) throws IOException
   {
      this.logDirectory = logDirectory;
      try
      {
         logDataReader = new LogDataReader(logDirectory, progressConsumer);
         LogTools.info("Created data reader.");
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      logProperties = logDataReader.getLogProperties();

      YoVariableHandshakeParser parser = logDataReader.getParser();
      rootRegistry.addChild(logDataReader.getYoVariableRegistry());
      rootRegistry.addChild(parser.getRootRegistry());

      sessionName = logProperties.getNameAsString();

      RobotDefinition robotDefinition = RobotDataLogTools.loadRobotDefinition(logDirectory, logProperties);

      if (robotDefinition != null)
      {
         robotDefinitions.add(robotDefinition);
         robotStateUpdater = RobotModelLoader.setupRobotUpdater(robotDefinition, parser, rootRegistry);
      }
      else
      {
         robotStateUpdater = null;
      }

      submitDesiredBufferPublishPeriod(Conversions.secondsToNanoseconds(1.0 / 30.0));
      setSessionTickToTimeIncrement(Conversions.secondsToNanoseconds(parser.getDt()));
      setSessionMode(SessionMode.PAUSE);
   }

   public void submitLogPositionRequest(int logPosition)
   {
      logPositionRequest.set(logPosition);
   }

   @Override
   protected void initializeRunTick()
   {
      if (firstRunTick)
      {
         sharedBuffer.setInPoint(sharedBuffer.getProperties().getCurrentIndex());
         firstRunTick = false;
      }

      // Push from the linked registries are unnecessary when reading a log file.
   }

   @Override
   protected void doSpecificRunTick()
   {
      boolean endOfLog = logDataReader.read();
      if (endOfLog)
         setSessionMode(SessionMode.PAUSE);

      if (robotStateUpdater != null)
         robotStateUpdater.run();
   }

   @Override
   public void pauseTick()
   {
      boolean shouldPublishBuffer = initializePauseTick();
      boolean isScrubbingThroughLog = doSpecificPauseTick();
      if (!isScrubbingThroughLog)
         finalizePauseTick(shouldPublishBuffer);
   }

   @Override
   protected boolean doSpecificPauseTick()
   {
      int logPosition = logPositionRequest.getAndSet(-1);

      if (logPosition == -1)
         return false;

      logDataReader.seek(logPosition);
      logDataReader.read();

      if (robotStateUpdater != null)
         robotStateUpdater.run();

      // TODO See if we can fall back to the default finalize
      sharedBuffer.updateBufferAndPublish();
      sharedBuffer.incrementBufferIndex(true);
      processBufferRequests(false);
      publishBufferProperties(sharedBuffer.getProperties());

      return true;
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

   public File getLogDirectory()
   {
      return logDirectory;
   }

   public LogDataReader getLogDataReader()
   {
      return logDataReader;
   }

   public LogPropertiesReader getLogProperties()
   {
      return logProperties;
   }
}
