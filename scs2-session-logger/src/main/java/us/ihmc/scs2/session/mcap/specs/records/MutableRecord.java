package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

public class MutableRecord implements Record
{
   private Opcode op;
   private Object body;
   private long elementLength = -1L;

   public void setOp(Opcode op)
   {
      this.op = op;
   }

   public void setBody(Object body)
   {
      elementLength = -1L;
      this.body = body;
   }

   private void updateElementLength()
   {
      // TODO fixme
      if (elementLength != -1L)
      {
         return;
      }
   }

   @Override
   public Opcode op()
   {
      return op;
   }

   @Override
   public <T> T body()
   {
      return (T) body;
   }

   @Override
   public void write(MCAPDataOutput dataOutput, boolean writeBody)
   {

   }

   @Override
   public long getElementLength()
   {
      updateElementLength();
      return elementLength;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }
}
