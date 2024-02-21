package us.ihmc.scs2.session.mcap.specs.records;

import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;

import java.util.Objects;

public class MutableRecord implements Record
{
   private Opcode op;
   private Object body;

   public MutableRecord()
   {
   }

   public MutableRecord(Object body)
   {
      this.op = body == null ? null : Opcode.byBodyType(body.getClass());
      this.body = body;
   }

   public void setOp(Opcode op)
   {
      this.op = op;
   }

   public void setBody(Object body)
   {
      this.body = body;
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
      dataOutput.putUnsignedByte(op == null ? 0 : op.id());
      dataOutput.putLong(bodyLength());

      if (writeBody)
      {
         if (body instanceof MCAPElement)
            ((MCAPElement) body).write(dataOutput);
         else if (body instanceof byte[])
            dataOutput.putBytes((byte[]) body);
         else
            throw new UnsupportedOperationException("Unsupported body type: " + body.getClass());
      }
   }

   @Override
   public long getElementLength()
   {
      return Record.RECORD_HEADER_LENGTH + bodyLength();
   }

   private long bodyLength()
   {
      Objects.requireNonNull(body);
      long bodyLength;
      if (body instanceof MCAPElement)
         bodyLength = ((MCAPElement) body).getElementLength();
      else if (body instanceof byte[])
         bodyLength = ((byte[]) body).length;
      else
         throw new UnsupportedOperationException("Unsupported body type: " + body.getClass());
      return bodyLength;
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public boolean equals(Object object)
   {
      return object instanceof Record other && Record.super.equals(other);
   }
}
