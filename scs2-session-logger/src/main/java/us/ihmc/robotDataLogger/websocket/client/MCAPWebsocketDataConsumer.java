package us.ihmc.robotDataLogger.websocket.client;

import io.netty.buffer.ByteBuf;
import us.ihmc.robotDataLogger.listeners.TimestampListener;
import us.ihmc.robotDataLogger.websocket.client.discovery.HTTPDataServerDescription;
import us.ihmc.robotDataLogger.websocket.client.discovery.HTTPMCAPDataServerConnection;
import us.ihmc.robotDataLogger.websocket.command.DataServerCommand;
import us.ihmc.robotDataLogger.websocket.dataBuffers.ConnectionStateListener;
import us.ihmc.robotDataLogger.websocket.dataBuffers.MCAPRegistryConsumer.MCAPSingleRecordConsumer;
import us.ihmc.robotDataLogger.websocket.server.MCAPDataServerServerContent;
import us.ihmc.scs2.session.mcap.input.MCAPNettyByteBufDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Attachment;
import us.ihmc.scs2.session.mcap.specs.records.Chunk;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MCAPWebsocketDataConsumer
{
   private final Object lock = new Object();
   private HTTPMCAPDataServerConnection connection;

   private MCAPWebsocketDataServerClient session;
   private boolean closed = false;

   private final int timeoutInMs;

   private TimestampListener timestampListener;
   private MCAPSingleRecordConsumer singleRecordConsumer;
   private ConnectionStateListener connectionStateListener;

   public MCAPWebsocketDataConsumer(HTTPMCAPDataServerConnection initialConnection, int timeoutInMs)
   {
      connection = initialConnection;
      this.timeoutInMs = timeoutInMs;
   }

   private ByteBuf getResource(String path) throws IOException
   {
      synchronized (lock)
      {
         if (!connection.isConnected())
         {
            throw new IOException("Not connected");
         }

         Future<ByteBuf> resourceFuture = connection.requestResource(path);

         try
         {
            return resourceFuture.get(timeoutInMs, TimeUnit.MILLISECONDS);
         }
         catch (Exception e)
         {
            throw new IOException(e);
         }
      }
   }

   public Attachment getResourceAttachment() throws IOException
   {
      ByteBuf resourceZip = getResource(MCAPDataServerServerContent.ROBOT_MODEL_RESOURCES);
      if (resourceZip == null)
         return null;
      MCAPNettyByteBufDataInput input = new MCAPNettyByteBufDataInput(resourceZip);
      return Attachment.load(input, 0, resourceZip.readableBytes());
   }

   public void startSession(TimestampListener timestampListener, MCAPSingleRecordConsumer singleRecordConsumer, ConnectionStateListener connectionStateListener)
         throws IOException
   {
      synchronized (lock)
      {
         if (!connection.isConnected())
         {
            throw new IOException("Not connected");
         }

         connection.take();
         this.timestampListener = timestampListener;
         this.singleRecordConsumer = singleRecordConsumer;
         this.connectionStateListener = connectionStateListener;

         session = new MCAPWebsocketDataServerClient(connection, timestampListener, singleRecordConsumer, connectionStateListener, timeoutInMs);
      }
   }

   public boolean isSessionActive()
   {
      synchronized (lock)
      {
         if (session == null)
         {
            return false;
         }
         else
         {
            return session.isActive();
         }
      }
   }

   public void disconnectSession()
   {
      synchronized (lock)
      {

         if (session == null)
         {
            throw new RuntimeException("Session not started");
         }

         session.close();
      }
   }

   public void close()
   {
      synchronized (lock)
      {

         if (connection.isConnected())
         {
            connection.close();
         }

         if (session != null)
         {
            if (session.isActive())
            {
               session.close();
            }
         }

         closed = true;
      }
   }

   public boolean isClosed()
   {
      synchronized (lock)
      {
         return closed;
      }
   }

   public boolean reconnect() throws IOException
   {
      synchronized (lock)
      {
         if (session != null && session.isActive())
         {
            throw new RuntimeException("Session is still active");
         }

         try
         {
            HTTPDataServerDescription oldDescription = connection.getTarget();
            HTTPMCAPDataServerConnection newConnection = HTTPMCAPDataServerConnection.connect(oldDescription.getHost(), oldDescription.getPort());
            newConnection.close();

            if (compareMCAPStarters(newConnection.getMCAPStarter(), connection.getMCAPStarter()))
            {
               connection = newConnection;
               session = new MCAPWebsocketDataServerClient(connection, timestampListener, singleRecordConsumer, connectionStateListener, timeoutInMs);
               return true;
            }
            else
            {
               return false;
            }
         }
         catch (IOException e)
         {
            System.err.println(e.getMessage());
            return false;
         }
      }
   }

   private static boolean compareMCAPStarters(MCAP mcap1, MCAP mcap2)
   {
      // We'll just compare: the version, the CRC32 of the schemas, channels, and the model.
      if (!Objects.equals(mcap1.header(), mcap2.header()))
         return false;

      List<Chunk> chunks1 = mcap1.records().stream().filter(record -> record instanceof Chunk).map(record -> (Chunk) record.body()).toList();
      List<Chunk> chunks2 = mcap2.records().stream().filter(record -> record instanceof Chunk).map(record -> (Chunk) record.body()).toList();

      if (chunks1.size() != chunks2.size())
         return false;

      Chunk schemas1 = chunks1.get(0);
      Chunk schemas2 = chunks2.get(0);

      if (schemas1.uncompressedCRC32() != schemas2.uncompressedCRC32())
         return false;

      Chunk channels1 = chunks1.get(1);
      Chunk channels2 = chunks2.get(1);

      if (channels1.uncompressedCRC32() != channels2.uncompressedCRC32())
         return false;

      Attachment robotModel1 = mcap1.records()
                                    .stream()
                                    .filter(record -> record instanceof Attachment)
                                    .map(record -> (Attachment) record.body())
                                    .filter(attachment -> attachment.mediaType().startsWith("model/"))
                                    .findFirst()
                                    .orElse(null);

      Attachment robotModel2 = mcap2.records()
                                    .stream()
                                    .filter(record -> record instanceof Attachment)
                                    .map(record -> (Attachment) record.body())
                                    .filter(attachment -> attachment.mediaType().startsWith("model/"))
                                    .findFirst()
                                    .orElse(null);

      if (robotModel1 == null && robotModel2 == null)
         return true;
      if (robotModel1 == null || robotModel2 == null)
         return false;
      if (robotModel1.crc32() != robotModel2.crc32())
         return false;
      return true;
   }

   // FIXME Fix this implementation
   public void writeVariableChangeRequest(int identifier, double valueAsDouble)
   {
      synchronized (lock)
      {
         if (session != null && session.isActive())
         {
            session.writeVariableChangeRequest(identifier, valueAsDouble);
         }
      }
   }

   public void sendCommand(DataServerCommand command, int argument) throws IOException
   {
      synchronized (lock)
      {
         if (session != null && session.isActive())
         {
            session.sendCommand(command, argument);
         }
      }
   }
}
