package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Chunk;
import us.ihmc.scs2.session.mcap.specs.records.Magic;
import us.ihmc.scs2.session.mcap.specs.records.MutableChunk;
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
            case METADATA_INDEX:
            {
               record.write(dataOutput, true);
               break;
            }
            case CHUNK:
            {
               Chunk chunk = (Chunk) record.body();
               if (chunk.messageEndTime() < startTimestamp)
               {
                  // Actually, we spend want to unpack stuff like SCHEMA, CHANNEL, METADATA, METADATA_INDEX, etc.
                  continue;
               }
               else if (chunk.messageStartTime() > endTimestamp)
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
                  long croppedStartTime = Math.max(chunk.messageStartTime(), startTimestamp);
                  long croppedEndTime = Math.min(chunk.messageEndTime(), endTimestamp);
                  Records croppedRecords = chunk.records().crop(croppedStartTime, croppedEndTime);
                  MutableChunk croppedChunk = new MutableChunk();
                  croppedChunk.setMessageStartTime(croppedStartTime);
                  croppedChunk.setMessageEndTime(croppedEndTime);
                  croppedChunk.setRecords(croppedRecords);
                  croppedChunk.setCompression(chunk.compression());
                  croppedChunk.setRecordsUncompressedLength(croppedRecords.getElementLength());
               }
            }
         }
      }
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();
   }
}
