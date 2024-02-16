package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import us.ihmc.commons.MathTools;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Attachment;
import us.ihmc.scs2.session.mcap.specs.records.Chunk;
import us.ihmc.scs2.session.mcap.specs.records.ChunkIndex;
import us.ihmc.scs2.session.mcap.specs.records.Magic;
import us.ihmc.scs2.session.mcap.specs.records.Message;
import us.ihmc.scs2.session.mcap.specs.records.MutableChunk;
import us.ihmc.scs2.session.mcap.specs.records.MutableChunkIndex;
import us.ihmc.scs2.session.mcap.specs.records.MutableRecord;
import us.ihmc.scs2.session.mcap.specs.records.Opcode;
import us.ihmc.scs2.session.mcap.specs.records.Record;
import us.ihmc.scs2.session.mcap.specs.records.Records;

import java.io.FileOutputStream;
import java.io.IOException;

public class MCAPLogCropper
{
   private final MCAP mcap;
   private long startTime;
   private long endTime;
   private OutputFormat outputFormat;

   public enum OutputFormat
   {
      MCAP
   }

   public MCAPLogCropper(MCAP mcap)
   {
      this.mcap = mcap;
   }

   public void setStartTime(long startTime)
   {
      this.startTime = startTime;
   }

   public void setEndTime(long endTime)
   {
      this.endTime = endTime;
   }

   public void setOutputFormat(OutputFormat outputFormat)
   {
      this.outputFormat = outputFormat;
   }

   public void crop(FileOutputStream outputStream, long startTimestamp, long endTimestamp) throws IOException
   {
      MCAPDataOutput dataOutput = MCAPDataOutput.wrap(outputStream.getChannel());
      dataOutput.putBytes(Magic.MAGIC_BYTES); // header magic

      for (Record record : mcap.records())
      {
         switch (record.op())
         {
            case HEADER:
            case FOOTER:
            case SCHEMA:
            case CHANNEL:
            case METADATA:
            case METADATA_INDEX: // TODO The indices should probably be recalculated
            case DATA_END: // TODO The CRC32 should probably be recalculated
            {
               record.write(dataOutput, true);
               break;
            }
            case CHUNK:
            {
               Chunk chunk = record.body();
               if (chunk.messageStartTime() > endTimestamp)
               {
                  continue;
               }
               else if (chunk.messageStartTime() > startTimestamp && chunk.messageEndTime() < endTimestamp)
               {
                  record.write(dataOutput, true);
               }
               else
               {
                  // Need to crop the chunk
                  long croppedStartTime = MathTools.clamp(chunk.messageStartTime(), startTimestamp, endTimestamp);
                  long croppedEndTime = MathTools.clamp(chunk.messageEndTime(), startTimestamp, endTimestamp);
                  Records croppedRecords = chunk.records().crop(croppedStartTime, croppedEndTime);
                  // There may be no records when testing a chunk that is before the start timestamp.
                  // We still want to test it in case there stuff like schemas, channels, and other time-insensitive data.
                  if (croppedRecords.isEmpty())
                     continue;
                  MutableChunk croppedChunk = new MutableChunk();
                  croppedChunk.setMessageStartTime(croppedStartTime);
                  croppedChunk.setMessageEndTime(croppedEndTime);
                  croppedChunk.setRecords(croppedRecords);
                  croppedChunk.setCompression(chunk.compression());
                  MutableRecord croppedRecord = new MutableRecord();
                  croppedRecord.setOp(Opcode.CHUNK);
                  croppedRecord.setBody(croppedChunk);
                  long chunkOffset = dataOutput.position();
                  croppedRecord.write(dataOutput, true);

                  MutableChunkIndex croppedChunkIndex = new MutableChunkIndex();
                  croppedChunkIndex.setChunkOffset(chunkOffset);
                  croppedChunkIndex.setChunk(croppedRecord);
               }
            }
            case MESSAGE:
            {
               Message message = record.body();
               if (message.logTime() >= startTimestamp && message.logTime() <= endTimestamp)
                  record.write(dataOutput, true);
            }
            case ATTACHMENT:
            {
               Attachment attachment = record.body();
               if (attachment.logTime() >= startTimestamp && attachment.logTime() <= endTimestamp)
                  record.write(dataOutput, true);
            }
            case CHUNK_INDEX:
            {
               // I think we want to re-build the chunk index from scratch.
            }
            ChunkIndex chunkIndex = record.body();
            if (chunkIndex.messageStartTime() > endTimestamp)
            {
               continue;
            }
            else if (chunkIndex.messageStartTime() > startTimestamp && chunkIndex.messageEndTime() < endTimestamp)
            {
               record.write(dataOutput, true);
            }
            else
            {
               // Need to crop the chunk
               long croppedStartTime = MathTools.clamp(chunkIndex.messageStartTime(), startTimestamp, endTimestamp);
               long croppedEndTime = MathTools.clamp(chunkIndex.messageEndTime(), startTimestamp, endTimestamp);
            }
         }
      }
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();
   }
}
