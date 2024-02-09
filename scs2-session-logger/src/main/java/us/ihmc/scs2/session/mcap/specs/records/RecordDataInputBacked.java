package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;

public class RecordDataInputBacked implements Record
{

   private final MCAPDataInput dataInput;

   private final Opcode op;
   private final long bodyLength;
   private final long bodyOffset;
   private WeakReference<Object> bodyRef;

   public RecordDataInputBacked(MCAPDataInput dataInput, long elementPosition)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      op = Opcode.byId(dataInput.getUnsignedByte());
      bodyLength = MCAP.checkPositiveLong(dataInput.getLong(), "bodyLength");
      bodyOffset = dataInput.position();
      MCAP.checkLength(getElementLength(), (int) (bodyLength + RECORD_HEADER_LENGTH));
   }

   @Override
   public void write(MCAPDataOutput dataOutput, boolean writeBody)
   {
      dataOutput.putUnsignedByte(op.id());
      dataOutput.putLong(bodyLength);
      if (writeBody)
         dataOutput.putBytes(dataInput.getBytes(bodyOffset, (int) bodyLength));
   }

   @Override
   public Opcode op()
   {
      return op;
   }

   @Override
   public long bodyOffset()
   {
      return bodyOffset;
   }

   @Override
   public long bodyLength()
   {
      return bodyLength;
   }

   @Override
   public Object body()
   {
      Object body = bodyRef == null ? null : bodyRef.get();

      if (body == null)
      {
         if (op == null)
         {
            body = dataInput.getBytes(bodyOffset, (int) bodyLength);
         }
         else
         {
            body = switch (op)
            {
               case MESSAGE -> Message.load(dataInput, bodyOffset, bodyLength);
               case METADATA_INDEX -> new MetadataIndex(dataInput, bodyOffset, bodyLength);
               case CHUNK -> Chunk.load(dataInput, bodyOffset, bodyLength);
               case SCHEMA -> Schema.load(dataInput, bodyOffset, bodyLength);
               case CHUNK_INDEX -> ChunkIndex.load(dataInput, bodyOffset, bodyLength);
               case DATA_END -> new DataEnd(dataInput, bodyOffset, bodyLength);
               case ATTACHMENT_INDEX -> AttachmentIndex.load(dataInput, bodyOffset, bodyLength);
               case STATISTICS -> Statistics.load(dataInput, bodyOffset, bodyLength);
               case MESSAGE_INDEX -> new MessageIndex(dataInput, bodyOffset, bodyLength);
               case CHANNEL -> Channel.load(dataInput, bodyOffset, bodyLength);
               case METADATA -> new Metadata(dataInput, bodyOffset, bodyLength);
               case ATTACHMENT -> Attachment.load(dataInput, bodyOffset, bodyLength);
               case HEADER -> new Header(dataInput, bodyOffset, bodyLength);
               case FOOTER -> Footer.load(dataInput, bodyOffset, bodyLength);
               case SUMMARY_OFFSET -> SummaryOffset.load(dataInput, bodyOffset, bodyLength);
            };
         }

         bodyRef = new WeakReference<>(body);
      }
      return body;
   }

   @Override
   public long getElementLength()
   {
      return RECORD_HEADER_LENGTH + bodyLength;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
