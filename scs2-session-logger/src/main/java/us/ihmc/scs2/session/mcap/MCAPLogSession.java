package us.ihmc.scs2.session.mcap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import gnu.trove.map.hash.TIntObjectHashMap;
import io.kaitai.struct.ByteBufferKaitaiStream;
import us.ihmc.scs2.definition.robot.RobotDefinition;
import us.ihmc.scs2.definition.robot.RobotStateDefinition;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.session.SessionMode;
import us.ihmc.scs2.session.mcap.Mcap.Channel;
import us.ihmc.scs2.session.mcap.Mcap.Chunk;
import us.ihmc.scs2.session.mcap.Mcap.Footer;
import us.ihmc.scs2.session.mcap.Mcap.Magic;
import us.ihmc.scs2.session.mcap.Mcap.Message;
import us.ihmc.scs2.session.mcap.Mcap.Opcode;
import us.ihmc.scs2.session.mcap.Mcap.Record;
import us.ihmc.scs2.session.mcap.Mcap.Records;
import us.ihmc.scs2.session.mcap.Mcap.Schema;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryTools;
import us.ihmc.scs2.simulation.robot.Robot;
import us.ihmc.yoVariables.registry.YoNamespace;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.tools.YoTools;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;

public class MCAPLogSession extends Session
{
   // FIXME Figure out how to name the session
   private final String sessionName = getClass().getSimpleName();
   private final List<Robot> robots = new ArrayList<>();
   private final List<RobotDefinition> robotDefinitions = new ArrayList<>();
   private final List<YoGraphicDefinition> yoGraphicDefinitions = new ArrayList<>();
   private final Runnable robotStateUpdater;
   private final TIntObjectHashMap<YoROS2Message> yoMessageMap = new TIntObjectHashMap<>();
   private Mcap mcap;

   private final YoRegistry mcapRegistry = new YoRegistry("MCAP");
   private final File mcapFile;

   private final List<Chunk> allChunks = new ArrayList<>();
   private final YoInteger chunkIndex = new YoInteger("MCAPChunkIndex", mcapRegistry);
   private final YoLong currentTimestamp = new YoLong("MCAPCurrentTimestamp", mcapRegistry);
   private final YoLong currentChunkStartTimestamp = new YoLong("MCAPCurrentChunkStartTimestamp", mcapRegistry);
   private final YoLong currentChunkEndTimestamp = new YoLong("MCAPCurrentChunkEndTimestamp", mcapRegistry);

   private final long initialTime;

   private final LZ4FrameDecoder chunkDataDecoder = new LZ4FrameDecoder();
   private LinkedList<Message> currentChunkMessages = new LinkedList<>();

   public MCAPLogSession(File mcapFile, MCAPDebugPrinter printer) throws IOException
   {
      // FIXME Do we need this guy?
      this.mcapFile = mcapFile;
      robotStateUpdater = null;
      mcap = Mcap.fromFile(mcapFile.getAbsolutePath());
      Magic headerMagic = mcap.headerMagic();
      byte[] magic = headerMagic.magic();
      byte[] restOfMagic = Arrays.copyOfRange(magic, 1, magic.length);
      printer.print("headerMagic = " + String.format("%02X", magic[0]) + " " + new String(restOfMagic, "UTF-8"));

      List<Record> records = mcap.records();
      printer.println("Number of records: " + records.size());

      TIntObjectHashMap<ROS2MessageSchema> schemas = new TIntObjectHashMap<>();

      for (Record record : records)
      {
         if (record.op() == Opcode.SCHEMA)
         {
            Schema schema = (Schema) record.body();
            try
            {
               schemas.put(schema.id(), ROS2MessageSchema.loadSchema(schema));
            }
            catch (Exception e)
            {
               File debugFile = new File("mcap-debug/schemas/" + schema.name().str() + "-schema.txt");
               debugFile.getParentFile().mkdirs();
               if (debugFile.exists())
                  debugFile.delete();
               debugFile.createNewFile();
               FileOutputStream os = new FileOutputStream(debugFile);
               os.write(schema.data());
               os.close();

               throw e;
            }
         }
      }

      List<YoROS2Message> loadedChannels = new ArrayList<>();

      for (Record record : records)
      {
         if (record.op() == Opcode.CHANNEL)
         {
            Channel channel = (Channel) record.body();
            YoROS2Message message = instantiateChannel(channel, schemas, mcapRegistry);
            loadedChannels.add(message);
            yoMessageMap.put(channel.id(), message);
         }
      }

      EnumSet<Opcode> alreadyPrintedOp = EnumSet.noneOf(Opcode.class);

      for (Record record : records)
      {
         Opcode op = record.op();

         //         if (alreadyPrintedOp.add(op))
         //         if (op == Opcode.SCHEMA)
         {
            printer.println(record.toString());
         }

         if (op == Opcode.CHUNK)
         {
            Chunk chunk = (Chunk) record.body();
            allChunks.add(chunk);
            System.out.println("From: %d, to: %d, duration: %f".formatted(chunk.messageStartTime(),
                                                                          chunk.messageEndTime(),
                                                                          (chunk.messageEndTime() - chunk.messageStartTime()) * 1.0e-9));
         }
      }

      initialTime = allChunks.get(0).messageStartTime();

      //      for (int i = 0; i < allChunks.size(); i++)
      //      {
      //         Chunk chunk = allChunks.get(i);
      //
      //         File file = new File("chunkData[%d]".formatted(i));
      //         if (file.exists())
      //            file.delete();
      //         file.createNewFile();
      //         FileOutputStream os = new FileOutputStream(file);
      //         os.write((byte[]) chunk.records());
      //         os.close();
      //      }

      Record footer = mcap.footer();
      if (footer != null)
      {
         Footer footerBody = (Footer) footer.body();
         long summaryCrc32 = footerBody.summaryCrc32();
         printer.println("Footer: summaryCrc32 = " + summaryCrc32);
         printer.println("");
      }

      rootRegistry.addChild(mcapRegistry);
   }

   private static YoROS2Message instantiateChannel(Channel channel, TIntObjectHashMap<ROS2MessageSchema> schemas, YoRegistry mcapRegistry)
   {
      if (!"cdr".equalsIgnoreCase(channel.messageEncoding().str()))
         throw new UnsupportedOperationException("Only CDR encoding is supported for now.");

      String topic = channel.topic().str();
      topic = topic.replace("/", YoTools.NAMESPACE_SEPERATOR_STRING);
      if (topic.startsWith(YoTools.NAMESPACE_SEPERATOR_STRING))
         topic = topic.substring(YoTools.NAMESPACE_SEPERATOR_STRING.length());
      YoNamespace namespace = new YoNamespace(topic);
      namespace = namespace.prepend(mcapRegistry.getNamespace());
      System.out.println(namespace);
      YoRegistry channelRegistry = SharedMemoryTools.ensurePathExists(mcapRegistry, namespace);
      YoROS2Message message = YoROS2Message.newMessage(schemas.get(channel.schemaId()), channel.id(), channelRegistry);
      return message;
   }

   @Override
   protected void initializeSession()
   {
      chunkIndex.set(0);
      Chunk chunk = allChunks.get(chunkIndex.getValue());
      currentTimestamp.set(chunk.messageStartTime());
      currentChunkStartTimestamp.set(chunk.messageStartTime());
      currentChunkEndTimestamp.set(chunk.messageEndTime());
      initializeMessages(chunk);
      Message message = currentChunkMessages.getFirst();
      if (chunk.messageStartTime() != message.logTime())
         throw new IllegalStateException("First message time (%d) does not match chunk start time (%d)".formatted(message.logTime(), chunk.messageStartTime()));

      readMessagesAtCurrentTimestamp();
   }

   @Override
   protected double doSpecificRunTick()
   {
      loadNextMessageBatch();
      return (currentTimestamp.getValue() - initialTime) * 1.0e-9;
   }

   private boolean loadNextMessageBatch()
   {
      if (currentChunkMessages.isEmpty())
      {
         // Load the next chunk
         chunkIndex.increment();
         if (chunkIndex.getValue() >= allChunks.size())
         {
            setSessionMode(SessionMode.PAUSE);
            return true;
         }
         Chunk chunk = allChunks.get(chunkIndex.getValue());
         currentTimestamp.set(chunk.messageStartTime());
         currentChunkStartTimestamp.set(chunk.messageStartTime());
         currentChunkEndTimestamp.set(chunk.messageEndTime());

         initializeMessages(chunk);

         Message message = currentChunkMessages.get(0);
         if (chunk.messageStartTime() != message.logTime())
            throw new IllegalStateException("First message time (%d) does not match chunk start time (%d)".formatted(message.logTime(),
                                                                                                                     chunk.messageStartTime()));
      }
      else
      {
         currentTimestamp.set(currentChunkMessages.peek().logTime());
      }

      readMessagesAtCurrentTimestamp();
      return false;
   }

   public void initializeMessages(Chunk chunk)
   {
      byte[] decompressedChunk = new byte[(int) chunk.uncompressedSize()];
      chunkDataDecoder.decode((byte[]) chunk.records(), decompressedChunk);
      Records records = new Records(new ByteBufferKaitaiStream(decompressedChunk));
      currentChunkMessages.clear();
      long previousMessageLogTime = -1;
      for (Record record : records.records())
      {
         if (record.op() != Opcode.MESSAGE)
            continue;
         Message message = (Message) record.body();

         if (previousMessageLogTime != -1)
         {
            if (previousMessageLogTime > message.logTime())
               throw new IllegalStateException("Messages are not sorted by time.");
         }
         currentChunkMessages.add(message);
         previousMessageLogTime = message.logTime();
      }
   }

   public void readMessagesAtCurrentTimestamp()
   {
      Message message = currentChunkMessages.getFirst();

      while (message.logTime() == currentTimestamp.getValue())
      {
         YoROS2Message yoROS2Message = yoMessageMap.get(message.channelId());
         if (yoROS2Message == null)
            throw new IllegalStateException("No YoROS2Message found for channel ID " + message.channelId());
         try
         {
            yoROS2Message.readMessage(message);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         currentChunkMessages.pollFirst();
         if (currentChunkMessages.isEmpty())
            break;
         message = currentChunkMessages.getFirst();
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

   @Override
   public List<RobotStateDefinition> getCurrentRobotStateDefinitions(boolean initialState)
   {
      return robots.stream().map(Robot::getCurrentRobotStateDefinition).collect(Collectors.toList());
   }

   public File getMCAPFile()
   {
      return mcapFile;
   }
}
