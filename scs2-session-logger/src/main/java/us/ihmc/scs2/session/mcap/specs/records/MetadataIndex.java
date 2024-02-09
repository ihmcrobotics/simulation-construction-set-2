package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;

public class MetadataIndex implements MCAPElement
{
   private final MCAPDataInput dataInput;
   private final long metadataOffset;
   private final long metadataLength;
   private final String name;
   private WeakReference<Record> metadataRef;

   MetadataIndex(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      metadataOffset = MCAP.checkPositiveLong(dataInput.getLong(), "metadataOffset");
      metadataLength = MCAP.checkPositiveLong(dataInput.getLong(), "metadataLength");
      name = dataInput.getString();
      MCAP.checkLength(elementLength, getElementLength());
   }

   @Override
   public long getElementLength()
   {
      return 2 * Long.BYTES + Integer.BYTES + name.length();
   }

   public Record metadata()
   {
      Record metadata = metadataRef == null ? null : metadataRef.get();

      if (metadata == null)
      {
         metadata = new RecordDataInputBacked(dataInput, metadataOffset);
         metadataRef = new WeakReference<>(metadata);
      }
      return metadata;
   }

   public long metadataOffset()
   {
      return metadataOffset;
   }

   public long metadataLength()
   {
      return metadataLength;
   }

   public String name()
   {
      return name;
   }

   @Override
   public String toString()
   {
      String out = getClass().getSimpleName() + ": ";
      out += "\n\t-metadataOffset = " + metadataOffset;
      out += "\n\t-metadataLength = " + metadataLength;
      out += "\n\t-name = " + name;
      return out;
   }
}
