package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import us.ihmc.scs2.session.mcap.MCAP;
import us.ihmc.scs2.session.mcap.MCAP.Magic;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Record;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;

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

   private final EnumSet<Opcode> opcodesToAlwaysExport = EnumSet.of(Opcode.HEADER,
                                                                    Opcode.FOOTER,
                                                                    Opcode.SCHEMA,
                                                                    Opcode.CHANNEL,
                                                                    Opcode.METADATA,
                                                                    Opcode.METADATA_INDEX);

   public void crop(FileOutputStream outputStream) throws IOException
   {
      MCAPDataOutput dataOutput = MCAPDataOutput.wrap(outputStream.getChannel());
      dataOutput.putBytes(Magic.MAGIC_BYTES); // header magic

      for (Record record : mcap.records())
      {
         if (opcodesToAlwaysExport.contains(record.op()))
         {
            record.write(dataOutput, true);
         }
      }
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();
   }
}
