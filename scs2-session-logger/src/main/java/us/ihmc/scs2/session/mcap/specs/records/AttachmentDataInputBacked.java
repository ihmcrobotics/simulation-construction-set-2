package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

class AttachmentDataInputBacked implements Attachment
{
   private final MCAPDataInput dataInput;
   private final long logTime;
   private final long createTime;
   private final String name;
   private final String mediaType;
   private final long dataLength;
   private final long dataOffset;
   private WeakReference<ByteBuffer> dataRef;
   private final long crc32;
   private final long crc32InputStart;
   private final int crc32InputLength;
   private WeakReference<ByteBuffer> crc32InputRef;

   AttachmentDataInputBacked(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      crc32InputStart = elementPosition;
      logTime = MCAP.checkPositiveLong(dataInput.getLong(), "logTime");
      createTime = MCAP.checkPositiveLong(dataInput.getLong(), "createTime");
      name = dataInput.getString();
      mediaType = dataInput.getString();
      dataLength = MCAP.checkPositiveLong(dataInput.getLong(), "lengthData");
      dataOffset = dataInput.position();
      dataInput.skip(dataLength);
      crc32InputLength = (int) (dataInput.position() - elementPosition);
      crc32 = dataInput.getUnsignedInt();
      MCAP.checkLength(elementLength, getElementLength());
   }

   @Override
   public long getElementLength()
   {
      return 3 * Long.BYTES + 3 * Integer.BYTES + name.length() + mediaType.length() + (int) dataLength;
   }

   @Override
   public ByteBuffer crc32Input()
   {
      ByteBuffer crc32Input = this.crc32InputRef == null ? null : this.crc32InputRef.get();

      if (crc32Input == null)
      {
         crc32Input = dataInput.getByteBuffer(crc32InputStart, crc32InputLength, false);
         crc32InputRef = new WeakReference<>(crc32Input);
      }

      return crc32Input;
   }

   @Override
   public long logTime()
   {
      return logTime;
   }

   @Override
   public long createTime()
   {
      return createTime;
   }

   @Override
   public String name()
   {
      return name;
   }

   @Override
   public String mediaType()
   {
      return mediaType;
   }

   public long dataOffset()
   {
      return dataOffset;
   }

   @Override
   public long dataLength()
   {
      return dataLength;
   }

   @Override
   public ByteBuffer data()
   {
      ByteBuffer data = this.dataRef == null ? null : this.dataRef.get();

      if (data == null)
      {
         data = dataInput.getByteBuffer(dataOffset, (int) dataLength, false);
         dataRef = new WeakReference<>(data);
      }
      return data;
   }

   @Override
   public long crc32()
   {
      return crc32;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
