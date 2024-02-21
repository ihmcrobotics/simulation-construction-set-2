package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.input.MCAPDataInput;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.lang.ref.WeakReference;

public class SummaryOffsetDataInputBacked implements SummaryOffset
{
   private final MCAPDataInput dataInput;
   private final Opcode groupOpcode;
   private final long groupOffset;
   private final long groupLength;

   private WeakReference<Records> groupRef;

   public SummaryOffsetDataInputBacked(MCAPDataInput dataInput, long elementPosition, long elementLength)
   {
      this.dataInput = dataInput;

      dataInput.position(elementPosition);
      groupOpcode = Opcode.byId(dataInput.getUnsignedByte());
      groupOffset = MCAP.checkPositiveLong(dataInput.getLong(), "offsetGroup");
      groupLength = MCAP.checkPositiveLong(dataInput.getLong(), "lengthGroup");
      MCAP.checkLength(elementLength, getElementLength());
   }

   @Override
   public long getElementLength()
   {
      return Byte.BYTES + 2 * Long.BYTES;
   }

   @Override
   public Records group()
   {
      Records group = groupRef == null ? null : groupRef.get();

      if (group == null)
      {
         group = Records.load(dataInput, groupOffset, (int) groupLength);
         groupRef = new WeakReference<>(group);
      }
      return group;
   }

   @Override
   public Opcode groupOpcode()
   {
      return groupOpcode;
   }

   @Override
   public long groupOffset()
   {
      return groupOffset;
   }

   @Override
   public long groupLength()
   {
      return groupLength;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public boolean equals(Object object)
   {
      return object instanceof SummaryOffset other && SummaryOffset.super.equals(other);
   }
}
