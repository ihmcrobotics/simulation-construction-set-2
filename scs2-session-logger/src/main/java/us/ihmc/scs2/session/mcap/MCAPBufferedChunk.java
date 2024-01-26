package us.ihmc.scs2.session.mcap;

import gnu.trove.map.hash.TLongObjectHashMap;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAP.Chunk;
import us.ihmc.scs2.session.mcap.MCAP.ChunkIndex;
import us.ihmc.scs2.session.mcap.MCAP.Message;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Record;
import us.ihmc.scs2.session.mcap.MCAP.Records;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static us.ihmc.scs2.session.mcap.MCAPMessageManager.round;

/**
 * This class is used to identify the chunks that need to be loaded and how many chunks can be loaded at the same time.
 */
public class MCAPBufferedChunk
{
   private static final double ALLOWABLE_CHUNK_MEMORY_RATIO = 0.025;
   private final long desiredLogDT;
   private final int maxNumberOfChunksLoaded;

   private final List<ChunkBundle> loadedChunkBundles = new ArrayList<>();
   private final ChunkBundle[] chunkBundles;
   private final ExecutorService executorService = Executors.newFixedThreadPool(4, ThreadTools.createNamedDaemonThreadFactory(getClass().getSimpleName()));

   public MCAPBufferedChunk(MCAP mcap, long desiredLogDT)
   {
      this.desiredLogDT = desiredLogDT;

      int numberOfChunks = 0;
      long averageChunkSize = 0;
      long minChunkSize = Long.MAX_VALUE;
      long maxChunkSize = Long.MIN_VALUE;
      long totalChunkSize = 0;

      List<ChunkIndex> orderedChunkIndices = new ArrayList<>();

      for (Record record : mcap.records())
      {
         if (record.op() == Opcode.CHUNK)
         {
            Chunk chunk = (Chunk) record.body();
            numberOfChunks++;
            long chunkSize = chunk.lenRecords();
            minChunkSize = Math.min(minChunkSize, chunkSize);
            maxChunkSize = Math.max(maxChunkSize, chunkSize);
            totalChunkSize += chunkSize;
            record.unloadBody();
         }
         else if (record.op() == Opcode.CHUNK_INDEX)
         {
            ChunkIndex chunkIndex = (ChunkIndex) record.body();
            orderedChunkIndices.add(chunkIndex);
         }
      }

      averageChunkSize = totalChunkSize / numberOfChunks;
      maxNumberOfChunksLoaded = (int) Math.ceil(ALLOWABLE_CHUNK_MEMORY_RATIO * Runtime.getRuntime().maxMemory() / averageChunkSize);
      LogTools.info("Chunk stats: [Average size: %d, min size: %d, max size: %d, total size: %d, quantity: %d], max memory: %d, max chunks loaded: %d".formatted(
            averageChunkSize,
            minChunkSize,
            maxChunkSize,
            totalChunkSize,
            numberOfChunks,
            Runtime.getRuntime().maxMemory(),
            maxNumberOfChunksLoaded));

      orderedChunkIndices.sort(Comparator.comparingLong(chunkIndex -> round(chunkIndex.messageStartTime(), desiredLogDT)));

      chunkBundles = new ChunkBundle[numberOfChunks];
      for (int i = 0; i < numberOfChunks; i++)
      {
         chunkBundles[i] = new ChunkBundle(i, orderedChunkIndices.get(i));
      }
   }

   public ChunkBundle[] getChunkBundles()
   {
      return chunkBundles;
   }

   public ChunkBundle getChunkBundle(long logTime)
   {
      int chunkIndex = searchChunkBundle(logTime);
      if (chunkIndex < 0)
         return null;
      else
         return chunkBundles[chunkIndex];
   }

   public void requestLoadChunk(long logTime, boolean wait)
   {
      ChunkBundle chunkBundle = getChunkBundle(logTime);
      chunkBundle.requestLoadChunk(wait);
   }

   public int getMaxNumberOfChunksLoaded()
   {
      return maxNumberOfChunksLoaded;
   }

   public int getNumberOfChunksLoaded()
   {
      return loadedChunkBundles.size();
   }

   private int searchChunkBundle(long timestamp)
   {
      if (chunkBundles.length == 0)
         return -1;

      int low = 0;
      int high = chunkBundles.length - 1;

      if (timestamp < round(chunkBundles[low].chunkIndex.messageStartTime(), desiredLogDT))
         return -1;
      if (timestamp > round(chunkBundles[high].chunkIndex.messageEndTime(), desiredLogDT))
         return -1;

      while (low <= high)
      {
         int mid = (low + high) >>> 1;
         ChunkBundle midVal = chunkBundles[mid];
         long midValStartTime = round(midVal.chunkIndex.messageStartTime(), desiredLogDT);

         if (timestamp == midValStartTime)
            return mid;

         if (timestamp > midValStartTime)
         {
            if (timestamp <= round(midVal.chunkIndex.messageEndTime(), desiredLogDT))
               return mid;
            else
               low = mid + 1;
         }
         else
         {
            high = mid - 1;
         }
      }
      return -1;
   }

   public ChunkBundle getChunkBundle(int chunkIndex)
   {
      return chunkBundles[chunkIndex];
   }

   public int getNumberOfChunks()
   {
      return chunkBundles.length;
   }

   public class ChunkBundle
   {
      private final int index;
      private final ChunkIndex chunkIndex;
      private Chunk chunk;
      private TLongObjectHashMap<List<Message>> bundledMessages;

      private volatile boolean isLoaded = false;
      private volatile boolean isLoading = false;
      private volatile CountDownLatch chunkLoadedLatch;
      private long lastLoadingRequestTime = Long.MIN_VALUE;

      public ChunkBundle(int index, ChunkIndex chunkIndex)
      {
         this.index = index;
         this.chunkIndex = chunkIndex;
      }

      public ChunkBundle next()
      {
         return index + 1 < chunkBundles.length ? chunkBundles[index + 1] : null;
      }

      public ChunkBundle previous()
      {
         return index > 0 ? chunkBundles[index - 1] : null;
      }

      private void freeUpChunkBundleSpots(int numberOfSpots)
      {
         while (loadedChunkBundles.size() > maxNumberOfChunksLoaded - numberOfSpots)
         {
            loadedChunkBundles.sort(Comparator.comparingLong(chunkBundle -> chunkBundle.lastLoadingRequestTime));
            loadedChunkBundles.get(0).unloadChunk();
         }
      }

      private void unloadChunk()
      {
         chunk = null;
         bundledMessages = null;
         loadedChunkBundles.remove(this);
         isLoaded = false;
      }

      public void requestLoadChunk(boolean wait)
      {
         lastLoadingRequestTime = System.nanoTime();

         if (isLoaded)
            return;

         if (!isLoading)
         {
            chunkLoadedLatch = new CountDownLatch(1);
            isLoading = true;

            freeUpChunkBundleSpots(1);

            executorService.submit(() ->
                                   {
                                      try
                                      {
                                         loadChunkImpl();
                                      }
                                      catch (Exception e)
                                      {
                                         e.printStackTrace();
                                         unloadChunk();
                                      }
                                      finally
                                      {
                                         isLoading = false;
                                         chunkLoadedLatch.countDown();
                                         chunkLoadedLatch = null;
                                      }
                                   });
         }

         try
         {
            if (chunkLoadedLatch != null && wait)
               chunkLoadedLatch.await();
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException(e);
         }
      }

      private void loadChunkImpl() throws IOException
      {
         if (chunk == null)
            chunk = (Chunk) chunkIndex.chunk().body();

         loadMessagesImpl();

         loadedChunkBundles.add(this);
         isLoaded = true;
      }

      private void loadMessagesImpl() throws IOException
      {
         Records records = chunk.records();

         for (Record record : records)
         {
            if (record.op() != Opcode.MESSAGE)
               continue;

            if (bundledMessages == null)
               bundledMessages = new TLongObjectHashMap<>();

            Message message = (Message) record.body();
            List<Message> messages = bundledMessages.get(round(message.logTime(), desiredLogDT));
            if (messages == null)
            {
               messages = new ArrayList<>();
               bundledMessages.put(round(message.logTime(), desiredLogDT), messages);
            }
            messages.add(message);
         }
      }

      public void forEachRecord(Opcode opcode, Consumer<Record> recordConsumer) throws IOException
      {
         Records records = chunk.records();

         for (Record record : records)
         {
            if (record.op() != opcode)
               continue;

            recordConsumer.accept(record);
         }
      }

      public long startTime()
      {
         return round(chunkIndex.messageStartTime(), desiredLogDT);
      }

      public long endTime()
      {
         return round(chunkIndex.messageEndTime(), desiredLogDT);
      }

      public List<Message> getMessages(long logTime)
      {
         if (!isLoaded)
            return null;
         if (bundledMessages == null)
         {
            try
            {
               loadMessagesImpl();
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         }
         return bundledMessages.get(round(logTime, desiredLogDT));
      }
   }
}
