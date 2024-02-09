package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Magic;
import us.ihmc.scs2.session.mcap.specs.records.Record;
import us.ihmc.scs2.session.mcap.specs.records.Chunk;

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

   public void crop(FileOutputStream outputStream) throws IOException
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
            }
         }
      }
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();
   }
}
