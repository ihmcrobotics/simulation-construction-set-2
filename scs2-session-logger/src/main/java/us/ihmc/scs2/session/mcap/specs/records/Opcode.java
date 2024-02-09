package us.ihmc.scs2.session.mcap.specs.records;

import gnu.trove.map.hash.TIntObjectHashMap;

public enum Opcode
{
   HEADER(1),
   FOOTER(2),
   SCHEMA(3),
   CHANNEL(4),
   MESSAGE(5),
   CHUNK(6),
   MESSAGE_INDEX(7),
   CHUNK_INDEX(8),
   ATTACHMENT(9),
   ATTACHMENT_INDEX(10),
   STATISTICS(11),
   METADATA(12),
   METADATA_INDEX(13),
   SUMMARY_OFFSET(14),
   DATA_END(15);

   private final int id;

   Opcode(int id)
   {
      this.id = id;
   }

   public int id()
   {
      return id;
   }

   private static final TIntObjectHashMap<Opcode> byId = new TIntObjectHashMap<>(15);

   static
   {
      for (Opcode e : Opcode.values())
         byId.put(e.id(), e);
   }

   public static Opcode byId(int id)
   {
      return byId.get(id);
   }
}
