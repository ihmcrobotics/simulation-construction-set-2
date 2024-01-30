package us.ihmc.scs2.session.mcap;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAP.Channel;
import us.ihmc.scs2.session.mcap.MCAP.Chunk;
import us.ihmc.scs2.session.mcap.MCAP.Message;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Record;
import us.ihmc.scs2.session.mcap.MCAP.Schema;
import us.ihmc.scs2.session.mcap.MCAPBufferedChunk.ChunkBundle;
import us.ihmc.scs2.simulation.SpyList;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MCAPConsoleLogManager
{
   public static final String FOXGLOVE_LOG = "foxglove::Log";
   private final long desiredLogDT;

   public enum MCAPLogLevel
   {UNKNOWN, DEBUG, INFO, WARNING, ERROR, FATAL}

   private final List<MCAPConsoleLogItem> allConsoleLogItems = new ArrayList<>();
   private final SpyList<MCAPConsoleLogItem> currentConsoleLogItems = new SpyList<>();

   private int channelId = -1;

   public MCAPConsoleLogManager(MCAP mcap, MCAPBufferedChunk chunkBuffer, long desiredLogDT) throws IOException
   {
      this.desiredLogDT = desiredLogDT;
      Optional<Schema> logSchema = mcap.records()
                                       .stream()
                                       .filter(record -> record.op() == Opcode.SCHEMA)
                                       .map(record -> (Schema) record.body())
                                       .filter(schema -> schema.name().equals(FOXGLOVE_LOG))
                                       .findFirst();

      if (logSchema.isPresent())
      {
         int id = logSchema.get().id();

         Optional<Channel> logChannel = mcap.records()
                                            .stream()
                                            .filter(record -> record.op() == Opcode.CHANNEL)
                                            .map(record -> (Channel) record.body())
                                            .filter(channel -> channel.schemaId() == id)
                                            .findFirst();
         if (logChannel.isPresent())
         {
            if (!"cdr".equalsIgnoreCase(logChannel.get().messageEncoding()))
            {
               LogTools.error("Unsupported message encoding: {}", logChannel.get().messageEncoding());
               return;
            }

            channelId = logChannel.get().id();

            Thread loadingThread = createLoadingThread(chunkBuffer, desiredLogDT);
            loadingThread.start();
         }
      }

      allConsoleLogItems.sort(Comparator.comparingLong(MCAPConsoleLogItem::logTime));
   }

   private Thread createLoadingThread(MCAPBufferedChunk chunkBuffer, long desiredLogDT)
   {
      Runnable loadingTask = () ->
      {
         for (ChunkBundle bundle : chunkBuffer.getChunkBundles())
         {
            try
            {
               Chunk chunk = (Chunk) bundle.getChunkIndex().chunk().body();
               List<MCAPConsoleLogItem> orderedItems = new ArrayList<>();
               for (Record record : chunk.records())
               {
                  if (record.op() != Opcode.MESSAGE)
                     continue;
                  Message message = (Message) record.body();
                  if (message.channelId() == channelId)
                     orderedItems.add(parseLogItem(message, desiredLogDT));
               }
               orderedItems.sort(Comparator.comparingLong(MCAPConsoleLogItem::logTime));
               allConsoleLogItems.addAll(orderedItems);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      };
      Thread loadingThread = new Thread(loadingTask, getClass().getSimpleName() + "-LoadingThread");
      loadingThread.setDaemon(true);
      return loadingThread;
   }

   public void update(long logTime)
   {
      if (channelId < 0)
         return;

      int allConsoleLogItemsSize = allConsoleLogItems.size();

      // First, if we actually need to update the currentConsoleLogItems
      if (currentConsoleLogItems.isEmpty())
      { // Case 1: We have no currentConsoleLogItems, but logTime is still before the first log item
         if (!allConsoleLogItems.isEmpty() && logTime < allConsoleLogItems.get(0).logTime())
            return;
      }
      else
      {
         if (currentConsoleLogItems.size() == allConsoleLogItemsSize)
         { // Case 2: We loaded all log items, and logTime is after the last log item
            if (logTime >= allConsoleLogItems.get(allConsoleLogItemsSize - 1).logTime())
               return;
         }
         else
         {// Case 3: We have some log items, and logTime is after the last loaded log item and before the next not-loaded log item
            int lastLoadedIndex = currentConsoleLogItems.size() - 1;
            int nextNotLoadedIndex = lastLoadedIndex + 1;
            if (logTime >= currentConsoleLogItems.get(lastLoadedIndex).logTime() && logTime < allConsoleLogItems.get(nextNotLoadedIndex).logTime())
               return;
         }
      }

      // Fixing the size to avoid the concurrent access.
      int index = binarySearch(allConsoleLogItems,
                               0,
                               allConsoleLogItemsSize,
                               new MCAPConsoleLogItem(logTime, null, null, null, null, null, 0),
                               Comparator.comparingLong(MCAPConsoleLogItem::logTime));
      if (index < 0)
         index = -index - 1;

      List<MCAPConsoleLogItem> updatedList = allConsoleLogItems.subList(0, index);
      if (currentConsoleLogItems.isEmpty())
      {
         currentConsoleLogItems.addAll(updatedList);
      }
      else if (currentConsoleLogItems.size() < updatedList.size())
      {
         int startIndex = currentConsoleLogItems.size();
         currentConsoleLogItems.addAll(updatedList.subList(startIndex, updatedList.size()));
      }
      else if (currentConsoleLogItems.size() > updatedList.size())
      {
         int endIndex = currentConsoleLogItems.size();
         currentConsoleLogItems.subList(updatedList.size(), endIndex).clear();
      }
   }

   /**
    * This method is a copy of {@link Collections#binarySearch(List, Object, Comparator)} while adding the options to specify the range of the list to search.
    *
    * @param list the list to be searched.
    * @param from the index of the first element (inclusive) to be searched.
    * @param to   the index of the last element (exclusive) to be searched.
    * @param key  the key to be searched for.
    * @param c    the comparator by which the list is ordered. A {@code null} value indicates that the elements' natural ordering should be used.
    * @param <T>  the type of elements in the list.
    * @return the index of the search key, if it is contained in the list; otherwise, <code>(-(<i>insertion point</i>) - 1)</code>.  The <i>insertion point</i>
    *       is defined as the point at which the key would be inserted into the list: the index of the first element greater than the key, or
    *       {@code list.size()} if all elements in the list are less than the specified key.  Note that this guarantees that the return value will be &gt;= 0 if
    *       and only if the key is found.
    */
   private static <T> int binarySearch(List<? extends T> list, int from, int to, T key, Comparator<? super T> c)
   {
      int low = from;
      int high = to - from - 1;

      while (low <= high)
      {
         int mid = (low + high) >>> 1;
         T midVal = list.get(mid);
         int cmp = c.compare(midVal, key);

         if (cmp < 0)
            low = mid + 1;
         else if (cmp > 0)
            high = mid - 1;
         else
            return mid; // key found
      }
      return -(low + 1);  // key not found
   }

   public SpyList<MCAPConsoleLogItem> getCurrentConsoleLogItems()
   {
      return currentConsoleLogItems;
   }

   private static MCAPConsoleLogItem parseLogItem(Message message, long desiredLogDT)
   {
      /*
       * @formatter:off
       * Expected schema:
       * struct type_010f744a0100bb8d02000000_030c3_1
       * {
       *     unsigned long sec;
       *     unsigned long nsec;
       * };
       *
       * struct foxglove::Log
       * {
       *     type_010f744a0100bb8d02000000_030c3_1 instant;
       *     octet level;
       *     string message;
       *     string name;
       *     string file;
       *     unsigned long line;
       * };
       * @formatter:on
       */
      long logTime = MCAPMessageManager.round(message.logTime(), desiredLogDT);
      CDRDeserializer deserializer = new CDRDeserializer();
      deserializer.initialize(message.messageBuffer(), message.offsetData(), message.lengthData());
      Instant instant = Instant.ofEpochSecond(deserializer.read_uint32(), deserializer.read_uint32());
      MCAPLogLevel logLevel = MCAPLogLevel.values()[deserializer.read_uint8()];
      String logMessage = deserializer.read_string();
      String processName = deserializer.read_string();
      String filename = deserializer.read_string();
      long lineNumberInFile = deserializer.read_uint32();
      deserializer.finalize(true);
      return new MCAPConsoleLogItem(logTime, instant, logLevel, logMessage, processName, filename, lineNumberInFile);
   }

   public record MCAPConsoleLogItem(long logTime, Instant instant, MCAPLogLevel logLevel, String message, String processName, String filename,
                                    long lineNumberInFile)
   {
   }
}
