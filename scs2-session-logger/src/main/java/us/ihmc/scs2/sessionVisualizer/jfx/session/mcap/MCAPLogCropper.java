package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Attachment;
import us.ihmc.scs2.session.mcap.specs.records.Chunk;
import us.ihmc.scs2.session.mcap.specs.records.Magic;
import us.ihmc.scs2.session.mcap.specs.records.Message;
import us.ihmc.scs2.session.mcap.specs.records.MutableRecord;
import us.ihmc.scs2.session.mcap.specs.records.Record;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

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
            case CHUNK_INDEX:
            case MESSAGE_INDEX:
            case ATTACHMENT_INDEX:
            case METADATA_INDEX:
            {
               // We re-build the indices from scratch down there.
               continue;
            }
            case HEADER:
            case FOOTER:
            case SCHEMA:
            case CHANNEL:
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
               else
               {
                  // We need to possibly crop the chunk and likely re-build the various indices.
                  long chunkOffset = dataOutput.position();

                  Chunk croppedChunk = chunk.crop(startTimestamp, endTimestamp);
                  if (croppedChunk == null)
                     continue;

                  MutableRecord croppedChunkRecord = new MutableRecord(croppedChunk);

                  List<MutableRecord> croppedMessageIndexRecords = croppedChunk.records().generateMessageIndexList().stream().map(MutableRecord::new).toList();

                  Record croppedChunkIndexRecord = croppedChunkRecord.generateChunkIndexRecord(chunkOffset, croppedMessageIndexRecords);

                  croppedChunkRecord.write(dataOutput, true);
                  croppedMessageIndexRecords.forEach(r -> r.write(dataOutput, true));
                  croppedChunkIndexRecord.write(dataOutput, true);
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
               {
                  long attachmentOffset = dataOutput.position();
                  record.write(dataOutput, true);
                  record.generateAttachmentIndexRecord(attachmentOffset).write(dataOutput, true);
               }
            }
            case METADATA:
            {
               long metadataOffset = dataOutput.position();
               record.write(dataOutput, true);
               record.generateMetadataIndexRecord(metadataOffset).write(dataOutput, true);
            }
         }
      }
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();
   }
}
