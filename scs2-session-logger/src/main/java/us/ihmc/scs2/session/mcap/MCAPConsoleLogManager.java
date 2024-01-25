package us.ihmc.scs2.session.mcap;

import org.apache.logging.log4j.Level;
import us.ihmc.scs2.session.mcap.MCAP.Channel;
import us.ihmc.scs2.session.mcap.MCAP.Opcode;
import us.ihmc.scs2.session.mcap.MCAP.Schema;
import us.ihmc.scs2.simulation.SpyList;

import java.util.Optional;

public class MCAPConsoleLogManager
{
   public static final String FOXGLOVE_LOG = "foxglove::Log";
   private final SpyList<MCAPConsoleLogItem> allConsoleLogItems = new SpyList<>();
   private final SpyList<MCAPConsoleLogItem> currentConsoleLogItems = new SpyList<>();

   public MCAPConsoleLogManager(MCAP mcap)
   {
      Optional<Schema> logSchema = mcap.records()
                                       .stream()
                                       .filter(record -> record.op() == Opcode.SCHEMA)
                                       .map(record -> (Schema) record.body())
                                       .filter(schema -> schema.name().equals(FOXGLOVE_LOG))
                                       .findFirst();

      if (logSchema.isPresent())
      {
         int id = logSchema.get().id();

         Optional<Channel> logChannel = mcap.records()
                                            .stream()
                                            .filter(record -> record.op() == Opcode.CHANNEL)
                                            .map(record -> (Channel) record.body())
                                            .filter(channel -> channel.schemaId() == id)
                                            .findFirst();
      }
   }

   public record MCAPConsoleLogItem(long timestamp, Level level, String message)
   {
   }
}
