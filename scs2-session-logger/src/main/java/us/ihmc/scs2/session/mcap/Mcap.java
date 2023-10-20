package us.ihmc.scs2.session.mcap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// This requires the project kaitai-struct-runtime, which is at github at
// https://github.com/kaitai-io/kaitai_struct_java_runtime

//This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStream;
import us.ihmc.euclid.tools.EuclidCoreIOTools;

/**
 * MCAP is a modular container format and logging library for pub/sub messages with arbitrary
 * message serialization. It is primarily intended for use in robotics applications, and works well
 * under various workloads, resource constraints, and durability requirements. Time values
 * (`log_time`, `publish_time`, `create_time`) are represented in nanoseconds since a
 * user-understood epoch (i.e. Unix epoch, robot boot time, etc.)
 *
 * @see <a href="https://github.com/foxglove/mcap/tree/c1cc51d/docs/specification#readme">Source</a>
 */
public class Mcap
{
   /**
    * Stream object that this KaitaiStruct-based structure was parsed from.
    */
   protected FileChannel _io;

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

      private final long id;

      Opcode(long id)
      {
         this.id = id;
      }

      public long id()
      {
         return id;
      }

      private static final Map<Long, Opcode> byId = new HashMap<>(15);

      static
      {
         for (Opcode e : Opcode.values())
            byId.put(e.id(), e);
      }

      public static Opcode byId(long id)
      {
         return byId.get(id);
      }
   }

   public Mcap(FileChannel _io)
   {
      this._io = _io;
      _read();
   }

   public FileChannel _io()
   {
      return _io;
   }

   private void _read()
   {
      this.headerMagic = new Magic(_io, 0, 8);
      this.records = new ArrayList<>();
      {
         Record _it;
         int i = 0;
         do
         {
            _it = new Record(this._io);
            this.records.add(_it);
            i++;
         }
         while (!(_it.op() == Opcode.FOOTER));
      }
      this.footerMagic = new Magic(this._io);
   }

   private static String indent(String stringToIndent, int indent)
   {
      if (indent <= 0)
         return stringToIndent;
      String indentStr = "\t".repeat(indent);
      return indentStr + stringToIndent.replace("\n", "\n" + indentStr);
   }

   public static class PrefixedStr extends KaitaiStructToStringEnabled
   {
      public PrefixedStr(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.lenStr = this._io.readU4le();
         this.str = new String(this._io.readBytes(lenStr()), Charset.forName("UTF-8"));
      }

      private long lenStr;
      private String str;

      public long lenStr()
      {
         return lenStr;
      }

      public String str()
      {
         return str;
      }

      @Override
      public String toString()
      {
         return str;
      }
   }

   public static class Chunk extends KaitaiStructToStringEnabled
   {
      public Chunk(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.messageStartTime = this._io.readU8le();
         this.messageEndTime = this._io.readU8le();
         this.uncompressedSize = this._io.readU8le();
         this.uncompressedCrc32 = this._io.readU4le();
         this.compression = new PrefixedStr(this._io);
         this.lenRecords = this._io.readU8le();
         switch (compression().str())
         {
            case "":
            {
               this._raw_records = this._io.readBytes(lenRecords());
               KaitaiStream _io__raw_records = new ByteBufferKaitaiStream(_raw_records);
               this.records = new Records(_io__raw_records);
               break;
            }
            default:
            {
               this.records = this._io.readBytes(lenRecords());
               break;
            }
         }
      }

      private long messageStartTime;
      private long messageEndTime;
      private long uncompressedSize;
      private long uncompressedCrc32;
      private PrefixedStr compression;
      private long lenRecords;
      private Object records;
      private byte[] _raw_records;

      public long messageStartTime()
      {
         return messageStartTime;
      }

      public long messageEndTime()
      {
         return messageEndTime;
      }

      public long uncompressedSize()
      {
         return uncompressedSize;
      }

      /**
       * CRC-32 checksum of uncompressed `records` field. A value of zero indicates that CRC validation
       * should not be performed.
       */
      public long uncompressedCrc32()
      {
         return uncompressedCrc32;
      }

      public PrefixedStr compression()
      {
         return compression;
      }

      public long lenRecords()
      {
         return lenRecords;
      }

      public Object records()
      {
         return records;
      }

      public byte[] _raw_records()
      {
         return _raw_records;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-messageStartTime = " + messageStartTime;
         out += "\n\t-messageEndTime = " + messageEndTime;
         out += "\n\t-compression = " + compression;
         out += "\n\t-compressedSize = " + lenRecords;
         out += "\n\t-uncompressedSize = " + uncompressedSize;
         out += "\n\t-uncompressedCrc32 = " + uncompressedCrc32;
         return out;
      }
   }

   public static class DataEnd extends KaitaiStructToStringEnabled
   {
      public DataEnd(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.dataSectionCrc32 = this._io.readU4le();
      }

      private long dataSectionCrc32;

      /**
       * CRC-32 of all bytes in the data section. A value of 0 indicates the CRC-32 is not available.
       */
      public long dataSectionCrc32()
      {
         return dataSectionCrc32;
      }

      @Override
      public String toString()
      {
         return getClass().getSimpleName() + ":\n\t-dataSectionCrc32 = " + dataSectionCrc32;
      }
   }

   public static class Channel extends KaitaiStructToStringEnabled
   {
      public Channel(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.id = this._io.readU2le();
         this.schemaId = this._io.readU2le();
         this.topic = new PrefixedStr(this._io);
         this.messageEncoding = new PrefixedStr(this._io);
         this.metadata = new MapStrStr(this._io);
      }

      private int id;
      private int schemaId;
      private PrefixedStr topic;
      private PrefixedStr messageEncoding;
      private MapStrStr metadata;

      public int id()
      {
         return id;
      }

      public int schemaId()
      {
         return schemaId;
      }

      public PrefixedStr topic()
      {
         return topic;
      }

      public PrefixedStr messageEncoding()
      {
         return messageEncoding;
      }

      public MapStrStr metadata()
      {
         return metadata;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-id = " + id;
         out += "\n\t-schemaId = " + schemaId;
         out += "\n\t-topic = " + topic;
         out += "\n\t-messageEncoding = " + messageEncoding;
         out += "\n\t-metadata = [%s]".formatted(metadata.toString());
         return out;
      }
   }

   public static class MessageIndex extends KaitaiStructToStringEnabled
   {
      public MessageIndex(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.channelId = this._io.readU2le();
         this.lenRecords = this._io.readU4le();
         this._raw_records = this._io.readBytes(lenRecords());
         KaitaiStream _io__raw_records = new ByteBufferKaitaiStream(_raw_records);
         this.records = new MessageIndexEntries(_io__raw_records);
      }

      public static class MessageIndexEntry extends KaitaiStructToStringEnabled
      {
         public MessageIndexEntry(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.logTime = this._io.readU8le();
            this.offset = this._io.readU8le();
         }

         private long logTime;
         private long offset;

         public long logTime()
         {
            return logTime;
         }

         public long offset()
         {
            return offset;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName() + ":";
            out += "\n\t-logTime = " + logTime;
            out += "\n\t-offset = " + offset;
            return indent(out, indent);
         }
      }

      public static class MessageIndexEntries extends KaitaiStructToStringEnabled
      {
         public MessageIndexEntries(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.entries = new ArrayList<>();
            {
               int i = 0;
               while (!this._io.isEof())
               {
                  this.entries.add(new MessageIndexEntry(this._io));
                  i++;
               }
            }
         }

         private ArrayList<MessageIndexEntry> entries;

         public ArrayList<MessageIndexEntry> entries()
         {
            return entries;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName();
            out += "\n\t-entries = " + (entries == null ? "null" : "\n" + EuclidCoreIOTools.getCollectionString("\n", entries, e -> e.toString(indent + 1)));
            return indent(out, indent);
         }
      }

      private int channelId;
      private long lenRecords;
      private MessageIndexEntries records;
      private byte[] _raw_records;

      public int channelId()
      {
         return channelId;
      }

      public long lenRecords()
      {
         return lenRecords;
      }

      public MessageIndexEntries records()
      {
         return records;
      }

      public byte[] _raw_records()
      {
         return _raw_records;
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      @Override
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-channelId = " + channelId;
         out += "\n\t-lenRecords = " + lenRecords;
         out += "\n\t-records = " + (records == null ? "null" : "\n" + records.toString(indent + 1));
         return indent(out, indent);
      }
   }

   public static class Statistics extends KaitaiStructToStringEnabled
   {
      public Statistics(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.messageCount = this._io.readU8le();
         this.schemaCount = this._io.readU2le();
         this.channelCount = this._io.readU4le();
         this.attachmentCount = this._io.readU4le();
         this.metadataCount = this._io.readU4le();
         this.chunkCount = this._io.readU4le();
         this.messageStartTime = this._io.readU8le();
         this.messageEndTime = this._io.readU8le();
         this.lenChannelMessageCounts = this._io.readU4le();
         this._raw_channelMessageCounts = this._io.readBytes(lenChannelMessageCounts());
         KaitaiStream _io__raw_channelMessageCounts = new ByteBufferKaitaiStream(_raw_channelMessageCounts);
         this.channelMessageCounts = new ChannelMessageCounts(_io__raw_channelMessageCounts);
      }

      public static class ChannelMessageCounts extends KaitaiStructToStringEnabled
      {
         public ChannelMessageCounts(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.entries = new ArrayList<>();
            {
               int i = 0;
               while (!this._io.isEof())
               {
                  this.entries.add(new ChannelMessageCount(this._io));
                  i++;
               }
            }
         }

         private ArrayList<ChannelMessageCount> entries;

         public ArrayList<ChannelMessageCount> entries()
         {
            return entries;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName() + ":";
            //            out += "\n\t-entries = " + (entries == null ? "null" : "\n" + EuclidCoreIOTools.getCollectionString("\n", entries, e -> e.toString(indent + 1)));
            return indent(out, indent);
         }
      }

      public static class ChannelMessageCount extends KaitaiStructToStringEnabled
      {
         public ChannelMessageCount(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.channelId = this._io.readU2le();
            this.messageCount = this._io.readU8le();
         }

         private int channelId;
         private long messageCount;

         public int channelId()
         {
            return channelId;
         }

         public long messageCount()
         {
            return messageCount;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName() + ":";
            out += "\n\t-channelId = " + channelId;
            out += "\n\t-messageCount = " + messageCount;
            return indent(out, indent);
         }
      }

      private long messageCount;
      private int schemaCount;
      private long channelCount;
      private long attachmentCount;
      private long metadataCount;
      private long chunkCount;
      private long messageStartTime;
      private long messageEndTime;
      private long lenChannelMessageCounts;
      private ChannelMessageCounts channelMessageCounts;
      private byte[] _raw_channelMessageCounts;

      public long messageCount()
      {
         return messageCount;
      }

      public int schemaCount()
      {
         return schemaCount;
      }

      public long channelCount()
      {
         return channelCount;
      }

      public long attachmentCount()
      {
         return attachmentCount;
      }

      public long metadataCount()
      {
         return metadataCount;
      }

      public long chunkCount()
      {
         return chunkCount;
      }

      public long messageStartTime()
      {
         return messageStartTime;
      }

      public long messageEndTime()
      {
         return messageEndTime;
      }

      public long lenChannelMessageCounts()
      {
         return lenChannelMessageCounts;
      }

      public ChannelMessageCounts channelMessageCounts()
      {
         return channelMessageCounts;
      }

      public byte[] _raw_channelMessageCounts()
      {
         return _raw_channelMessageCounts;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-messageCount = " + messageCount;
         out += "\n\t-schemaCount = " + schemaCount;
         out += "\n\t-channelCount = " + channelCount;
         out += "\n\t-attachmentCount = " + attachmentCount;
         out += "\n\t-metadataCount = " + metadataCount;
         out += "\n\t-chunkCount = " + chunkCount;
         out += "\n\t-messageStartTime = " + messageStartTime;
         out += "\n\t-messageEndTime = " + messageEndTime;
         out += "\n\t-lenChannelMessageCounts = " + lenChannelMessageCounts;
         out += "\n\t-channelMessageCounts = \n" + channelMessageCounts.toString(1);
         return out;
      }
   }

   public static class AttachmentIndex extends KaitaiStructToStringEnabled
   {
      public AttachmentIndex(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.ofsAttachment = this._io.readU8le();
         this.lenAttachment = this._io.readU8le();
         this.logTime = this._io.readU8le();
         this.createTime = this._io.readU8le();
         this.dataSize = this._io.readU8le();
         this.name = new PrefixedStr(this._io);
         this.mediaType = new PrefixedStr(this._io);
      }

      private Record attachment;

      public Record attachment()
      {
         if (this.attachment != null)
            return this.attachment;
         KaitaiStream io = _root()._io();
         long _pos = io.pos();
         io.seek(ofsAttachment());
         this._raw_attachment = io.readBytes(lenAttachment());
         KaitaiStream _io__raw_attachment = new ByteBufferKaitaiStream(_raw_attachment);
         this.attachment = new Record(_io__raw_attachment);
         io.seek(_pos);
         return this.attachment;
      }

      private long ofsAttachment;
      private long lenAttachment;
      private long logTime;
      private long createTime;
      private long dataSize;
      private PrefixedStr name;
      private PrefixedStr mediaType;
      private byte[] _raw_attachment;

      public long ofsAttachment()
      {
         return ofsAttachment;
      }

      public long lenAttachment()
      {
         return lenAttachment;
      }

      public long logTime()
      {
         return logTime;
      }

      public long createTime()
      {
         return createTime;
      }

      public long dataSize()
      {
         return dataSize;
      }

      public PrefixedStr name()
      {
         return name;
      }

      public PrefixedStr mediaType()
      {
         return mediaType;
      }

      public byte[] _raw_attachment()
      {
         return _raw_attachment;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-ofsAttachment = " + ofsAttachment;
         out += "\n\t-lenAttachment = " + lenAttachment;
         out += "\n\t-logTime = " + logTime;
         out += "\n\t-createTime = " + createTime;
         out += "\n\t-dataSize = " + dataSize;
         out += "\n\t-name = " + name;
         out += "\n\t-mediaType = " + mediaType;
         return out;
      }
   }

   public static class Schema extends KaitaiStructToStringEnabled
   {
      public Schema(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.id = this._io.readU2le();
         this.name = new PrefixedStr(this._io);
         this.encoding = new PrefixedStr(this._io);
         this.lenData = this._io.readU4le();
         this.data = this._io.readBytes(lenData());
      }

      private int id;
      private PrefixedStr name;
      private PrefixedStr encoding;
      private long lenData;
      private byte[] data;

      public int id()
      {
         return id;
      }

      public PrefixedStr name()
      {
         return name;
      }

      public PrefixedStr encoding()
      {
         return encoding;
      }

      public long lenData()
      {
         return lenData;
      }

      public byte[] data()
      {
         return data;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-id = " + id;
         out += "\n\t-name = " + name;
         out += "\n\t-encoding = " + encoding;
         out += "\n\t-lenData = " + lenData;
         out += "\n\t-data = " + data;
         return out;
      }
   }

   public static class MapStrStr extends KaitaiStructToStringEnabled
   {
      public MapStrStr(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.lenEntries = this._io.readU4le();
         this._raw_entries = this._io.readBytes(lenEntries());
         KaitaiStream _io__raw_entries = new ByteBufferKaitaiStream(_raw_entries);
         this.entries = new Entries(_io__raw_entries);
      }

      public static class Entries extends KaitaiStructToStringEnabled
      {
         public Entries(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.entries = new ArrayList<>();
            {
               int i = 0;
               while (!this._io.isEof())
               {
                  this.entries.add(new TupleStrStr(this._io));
                  i++;
               }
            }
         }

         private ArrayList<TupleStrStr> entries;

         public ArrayList<TupleStrStr> entries()
         {
            return entries;
         }

         @Override
         public String toString()
         {
            if (entries == null)
               return "null";
            return EuclidCoreIOTools.getCollectionString(", ", entries, e -> "(%s)".formatted(Objects.toString(e)));
         }

         public String toKeysString()
         {
            return EuclidCoreIOTools.getCollectionString(", ", entries, e -> e.key().str());
         }
      }

      private long lenEntries;
      private Entries entries;
      private byte[] _raw_entries;

      public long lenEntries()
      {
         return lenEntries;
      }

      public Entries entries()
      {
         return entries;
      }

      public byte[] _raw_entries()
      {
         return _raw_entries;
      }

      @Override
      public String toString()
      {
         return Objects.toString(entries);
      }

      public String toKeysString()
      {
         return entries.toKeysString();
      }
   }

   public static class SummaryOffset extends KaitaiStructToStringEnabled
   {
      public SummaryOffset(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.groupOpcode = Opcode.byId(this._io.readU1());
         this.ofsGroup = this._io.readU8le();
         this.lenGroup = this._io.readU8le();
      }

      private Records group;

      public Records group()
      {
         if (this.group != null)
            return this.group;
         KaitaiStream io = _root()._io();
         long _pos = io.pos();
         io.seek(ofsGroup());
         this._raw_group = io.readBytes(lenGroup());
         KaitaiStream _io__raw_group = new ByteBufferKaitaiStream(_raw_group);
         this.group = new Records(_io__raw_group);
         io.seek(_pos);
         return this.group;
      }

      private Opcode groupOpcode;
      private long ofsGroup;
      private long lenGroup;
      private byte[] _raw_group;

      public Opcode groupOpcode()
      {
         return groupOpcode;
      }

      public long ofsGroup()
      {
         return ofsGroup;
      }

      public long lenGroup()
      {
         return lenGroup;
      }

      public byte[] _raw_group()
      {
         return _raw_group;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-groupOpcode = " + groupOpcode;
         out += "\n\t-ofsGroup = " + ofsGroup;
         out += "\n\t-lenGroup = " + lenGroup;
         return out;
      }
   }

   public static class Attachment extends KaitaiStructToStringEnabled
   {
      public Attachment(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.logTime = this._io.readU8le();
         this.createTime = this._io.readU8le();
         this.name = new PrefixedStr(this._io);
         this.mediaType = new PrefixedStr(this._io);
         this.lenData = this._io.readU8le();
         this.data = this._io.readBytes(lenData());
         if (crc32InputEnd() >= 0)
         {
            this.invokeCrc32InputEnd = this._io.readBytes(0);
         }
         this.crc32 = this._io.readU4le();
      }

      private Integer crc32InputEnd;

      public Integer crc32InputEnd()
      {
         if (this.crc32InputEnd != null)
            return this.crc32InputEnd;
         int _tmp = (_io.pos());
         this.crc32InputEnd = _tmp;
         return this.crc32InputEnd;
      }

      private byte[] crc32Input;

      public byte[] crc32Input()
      {
         if (this.crc32Input != null)
            return this.crc32Input;
         long _pos = this._io.pos();
         this._io.seek(0);
         this.crc32Input = this._io.readBytes(crc32InputEnd());
         this._io.seek(_pos);
         return this.crc32Input;
      }

      private long logTime;
      private long createTime;
      private PrefixedStr name;
      private PrefixedStr mediaType;
      private long lenData;
      private byte[] data;
      private byte[] invokeCrc32InputEnd;
      private long crc32;

      public long logTime()
      {
         return logTime;
      }

      public long createTime()
      {
         return createTime;
      }

      public PrefixedStr name()
      {
         return name;
      }

      public PrefixedStr mediaType()
      {
         return mediaType;
      }

      public long lenData()
      {
         return lenData;
      }

      public byte[] data()
      {
         return data;
      }

      public byte[] invokeCrc32InputEnd()
      {
         return invokeCrc32InputEnd;
      }

      /**
       * CRC-32 checksum of preceding fields in the record. A value of zero indicates that CRC validation
       * should not be performed.
       */
      public long crc32()
      {
         return crc32;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-logTime = " + logTime;
         out += "\n\t-createTime = " + createTime;
         out += "\n\t-name = " + name;
         out += "\n\t-mediaType = " + mediaType;
         out += "\n\t-lenData = " + lenData;
         //         out += "\n\t-data = " + data;
         out += "\n\t-invokeCrc32InputEnd = " + invokeCrc32InputEnd;
         out += "\n\t-crc32 = " + crc32;
         return out;
      }
   }

   public static class Metadata extends KaitaiStructToStringEnabled
   {
      public Metadata(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.name = new PrefixedStr(this._io);
         this.metadata = new MapStrStr(this._io);
      }

      private PrefixedStr name;
      private MapStrStr metadata;

      public PrefixedStr name()
      {
         return name;
      }

      public MapStrStr metadata()
      {
         return metadata;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-name = " + name;
         out += "\n\t-metadata = " + metadata.toKeysString();
         return out;
      }
   }

   public static class Header extends KaitaiStructToStringEnabled
   {
      public Header(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.profile = new PrefixedStr(this._io);
         this.library = new PrefixedStr(this._io);
      }

      private PrefixedStr profile;
      private PrefixedStr library;

      public PrefixedStr profile()
      {
         return profile;
      }

      public PrefixedStr library()
      {
         return library;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-profile = " + profile;
         out += "\n\t-library = " + library;
         return out;
      }
   }

   public static class Message extends KaitaiStructToStringEnabled
   {
      public Message(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.channelId = this._io.readU2le();
         this.sequence = this._io.readU4le();
         this.logTime = this._io.readU8le();
         this.publishTime = this._io.readU8le();
         this.data = this._io.readBytesFull();
      }

      private int channelId;
      private long sequence;
      private long logTime;
      private long publishTime;
      private byte[] data;

      public int channelId()
      {
         return channelId;
      }

      public long sequence()
      {
         return sequence;
      }

      public long logTime()
      {
         return logTime;
      }

      public long publishTime()
      {
         return publishTime;
      }

      public byte[] data()
      {
         return data;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-channelId = " + channelId;
         out += "\n\t-sequence = " + sequence;
         out += "\n\t-logTime = " + logTime;
         out += "\n\t-publishTime = " + publishTime;
         //         out += "\n\t-data = " + data;
         return out;
      }
   }

   public static class TupleStrStr extends KaitaiStructToStringEnabled
   {
      public TupleStrStr(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.key = new PrefixedStr(this._io);
         this.value = new PrefixedStr(this._io);
      }

      private PrefixedStr key;
      private PrefixedStr value;

      public PrefixedStr key()
      {
         return key;
      }

      public PrefixedStr value()
      {
         return value;
      }

      @Override
      public String toString()
      {
         return (key.str() + ": " + value.str()).replace("\n", "");
      }
   }

   public static class MetadataIndex extends KaitaiStructToStringEnabled
   {
      public MetadataIndex(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.ofsMetadata = this._io.readU8le();
         this.lenMetadata = this._io.readU8le();
         this.name = new PrefixedStr(this._io);
      }

      private Record metadata;

      public Record metadata()
      {
         if (this.metadata != null)
            return this.metadata;
         KaitaiStream io = _root()._io();
         long _pos = io.pos();
         io.seek(ofsMetadata());
         this._raw_metadata = io.readBytes(lenMetadata());
         KaitaiStream _io__raw_metadata = new ByteBufferKaitaiStream(_raw_metadata);
         this.metadata = new Record(_io__raw_metadata);
         io.seek(_pos);
         return this.metadata;
      }

      private long ofsMetadata;
      private long lenMetadata;
      private PrefixedStr name;
      private byte[] _raw_metadata;

      public long ofsMetadata()
      {
         return ofsMetadata;
      }

      public long lenMetadata()
      {
         return lenMetadata;
      }

      public PrefixedStr name()
      {
         return name;
      }

      public byte[] _raw_metadata()
      {
         return _raw_metadata;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-ofsMetadata = " + ofsMetadata;
         out += "\n\t-lenMetadata = " + lenMetadata;
         out += "\n\t-name = " + name;
         return out;
      }
   }

   public static class Magic extends KaitaiStructToStringEnabled
   {
      private final ByteBuffer magic;

      public Magic(FileChannel _io, long _pos, int _length) throws IOException
      {
         super(_io, _pos, _length);
         magic = ByteBuffer.allocate(_length);
         _read();
      }

      @Override
      public void _read() throws IOException
      {
         _io.position(_pos);
         _io.read(magic);
         if (!(Arrays.equals(magic(), new byte[] {-119, 77, 67, 65, 80, 48, 13, 10})))
         {
            throw new KaitaiStream.ValidationNotEqualError(new byte[] {-119, 77, 67, 65, 80, 48, 13, 10}, magic(), _io(), "/types/magic/seq/0");
         }
      }

      public byte[] magic()
      {
         return magic.array();
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      @Override
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-magic = " + magic;
         return indent(out, indent);
      }
   }

   public static class Records extends KaitaiStructToStringEnabled
   {
      public Records(FileChannel _io, long _pos, int _length) throws IOException
      {
         super(_io, _pos, _length);
         _read();
      }

      @Override
      private void _read()
      {
         this.records = new ArrayList<>();
         {
            int i = 0;
            while (!this._io.isEof())
            {

               this.records.add(new Record(this._io));
               i++;
            }
         }
      }

      private ArrayList<Record> records;

      public ArrayList<Record> records()
      {
         return records;
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      @Override
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-records = " + (records == null ? "null" : "\n" + EuclidCoreIOTools.getCollectionString("\n", records, r -> r.toString(indent + 1)));
         return indent(out, indent);
      }
   }

   public static class Footer extends KaitaiStructToStringEnabled
   {
      private final ByteBuffer buffer;

      public Footer(FileChannel _io, long _pos, int _length) throws IOException
      {
         super(_io, _pos, _length);
         _read();
         buffer = ByteBuffer.allocate(_length);
         buffer.order(ByteOrder.LITTLE_ENDIAN);
      }

      @Override
      public void _read() throws IOException
      {
         _io.position(_pos);
         _io.read(buffer);
         this.ofsSummarySection = buffer.getLong();
         this.ofsSummaryOffsetSection = buffer.getLong();
         this.summaryCrc32 = Integer.toUnsignedLong(buffer.getInt());
      }

      private Records summarySection;

      public Records summarySection()
      {
         if (this.summarySection != null)
            return this.summarySection;
         if (ofsSummarySection() != 0)
         {
            KaitaiStream io = _root()._io();
            long _pos = io.pos();
            io.seek(ofsSummarySection());
            this._raw_summarySection = io.readBytes(((ofsSummaryOffsetSection() != 0 ? ofsSummaryOffsetSection() : _root().ofsFooter()) - ofsSummarySection()));
            KaitaiStream _io__raw_summarySection = new ByteBufferKaitaiStream(_raw_summarySection);
            this.summarySection = new Records(_io__raw_summarySection);
            io.seek(_pos);
         }
         return this.summarySection;
      }

      private Records summaryOffsetSection;

      public Records summaryOffsetSection()
      {
         if (this.summaryOffsetSection != null)
            return this.summaryOffsetSection;
         if (ofsSummaryOffsetSection() != 0)
         {
            KaitaiStream io = _root()._io();
            long _pos = io.pos();
            io.seek(ofsSummaryOffsetSection());
            this._raw_summaryOffsetSection = io.readBytes((_root().ofsFooter() - ofsSummaryOffsetSection()));
            KaitaiStream _io__raw_summaryOffsetSection = new ByteBufferKaitaiStream(_raw_summaryOffsetSection);
            this.summaryOffsetSection = new Records(_io__raw_summaryOffsetSection);
            io.seek(_pos);
         }
         return this.summaryOffsetSection;
      }

      private Integer ofsSummaryCrc32Input;

      public Integer ofsSummaryCrc32Input()
      {
         if (this.ofsSummaryCrc32Input != null)
            return this.ofsSummaryCrc32Input;
         int _tmp = (int) ((ofsSummarySection() != 0 ? ofsSummarySection() : _root().ofsFooter()));
         this.ofsSummaryCrc32Input = _tmp;
         return this.ofsSummaryCrc32Input;
      }

      private byte[] summaryCrc32Input;

      public byte[] summaryCrc32Input()
      {
         if (this.summaryCrc32Input != null)
            return this.summaryCrc32Input;
         KaitaiStream io = _root()._io();
         long _pos = io.pos();
         io.seek(ofsSummaryCrc32Input());
         this.summaryCrc32Input = io.readBytes((((_root()._io().size() - ofsSummaryCrc32Input()) - 8) - 4));
         io.seek(_pos);
         return this.summaryCrc32Input;
      }

      private long ofsSummarySection;
      private long ofsSummaryOffsetSection;
      private long summaryCrc32;
      private byte[] _raw_summarySection;
      private byte[] _raw_summaryOffsetSection;

      public long ofsSummarySection()
      {
         return ofsSummarySection;
      }

      public long ofsSummaryOffsetSection()
      {
         return ofsSummaryOffsetSection;
      }

      /**
       * A CRC-32 of all bytes from the start of the Summary section up through and including the end of
       * the previous field (summary_offset_start) in the footer record. A value of 0 indicates the CRC-32
       * is not available.
       */
      public long summaryCrc32()
      {
         return summaryCrc32;
      }

      public byte[] _raw_summarySection()
      {
         return _raw_summarySection;
      }

      public byte[] _raw_summaryOffsetSection()
      {
         return _raw_summaryOffsetSection;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-ofsSummarySection = " + ofsSummarySection;
         out += "\n\t-ofsSummaryOffsetSection = " + ofsSummaryOffsetSection;
         out += "\n\t-summaryCrc32 = " + summaryCrc32;
         return out;
      }
   }

   public static class Record extends KaitaiStructToStringEnabled
   {
      public Record(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.op = Opcode.byId(this._io.readU1());
         this.lenBody = this._io.readU8le();
         {
            Opcode on = op();
            if (on != null)
            {
               switch (op())
               {
                  case MESSAGE:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Message(_io__raw_body);
                     break;
                  }
                  case METADATA_INDEX:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new MetadataIndex(_io__raw_body);
                     break;
                  }
                  case CHUNK:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Chunk(_io__raw_body);
                     break;
                  }
                  case SCHEMA:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Schema(_io__raw_body);
                     break;
                  }
                  case CHUNK_INDEX:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new ChunkIndex(_io__raw_body);
                     break;
                  }
                  case DATA_END:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new DataEnd(_io__raw_body);
                     break;
                  }
                  case ATTACHMENT_INDEX:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new AttachmentIndex(_io__raw_body);
                     break;
                  }
                  case STATISTICS:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Statistics(_io__raw_body);
                     break;
                  }
                  case MESSAGE_INDEX:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new MessageIndex(_io__raw_body);
                     break;
                  }
                  case CHANNEL:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Channel(_io__raw_body);
                     break;
                  }
                  case METADATA:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Metadata(_io__raw_body);
                     break;
                  }
                  case ATTACHMENT:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Attachment(_io__raw_body);
                     break;
                  }
                  case HEADER:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Header(_io__raw_body);
                     break;
                  }
                  case FOOTER:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new Footer(_io__raw_body);
                     break;
                  }
                  case SUMMARY_OFFSET:
                  {
                     this._raw_body = this._io.readBytes(lenBody());
                     KaitaiStream _io__raw_body = new ByteBufferKaitaiStream(_raw_body);
                     this.body = new SummaryOffset(_io__raw_body);
                     break;
                  }
                  default:
                  {
                     this.body = this._io.readBytes(lenBody());
                     break;
                  }
               }
            }
            else
            {
               this.body = this._io.readBytes(lenBody());
            }
         }
      }

      private Opcode op;
      private long lenBody;
      private Object body;
      private byte[] _raw_body;

      public Opcode op()
      {
         return op;
      }

      public long lenBody()
      {
         return lenBody;
      }

      public Object body()
      {
         return body;
      }

      public byte[] _raw_body()
      {
         return _raw_body;
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      @Override
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-op = " + op;
         out += "\n\t-lenBody = " + lenBody;
         out += "\n\t-body = " + (body == null ? "null" : "\n" + ((KaitaiStructToStringEnabled) body).toString(indent + 2));
         return indent(out, indent);
      }
   }

   public static class ChunkIndex extends KaitaiStructToStringEnabled
   {
      public ChunkIndex(KaitaiStream _io)
      {
         super(_io);
         _read();
      }

      @Override
      private void _read()
      {
         this.messageStartTime = this._io.readU8le();
         this.messageEndTime = this._io.readU8le();
         this.ofsChunk = this._io.readU8le();
         this.lenChunk = this._io.readU8le();
         this.lenMessageIndexOffsets = this._io.readU4le();
         this._raw_messageIndexOffsets = this._io.readBytes(lenMessageIndexOffsets());
         KaitaiStream _io__raw_messageIndexOffsets = new ByteBufferKaitaiStream(_raw_messageIndexOffsets);
         this.messageIndexOffsets = new MessageIndexOffsets(_io__raw_messageIndexOffsets);
         this.messageIndexLength = this._io.readU8le();
         this.compression = new PrefixedStr(this._io);
         this.compressedSize = this._io.readU8le();
         this.uncompressedSize = this._io.readU8le();
      }

      public static class MessageIndexOffset extends KaitaiStructToStringEnabled
      {
         public MessageIndexOffset(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.channelId = this._io.readU2le();
            this.offset = this._io.readU8le();
         }

         private int channelId;
         private long offset;

         public int channelId()
         {
            return channelId;
         }

         public long offset()
         {
            return offset;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName() + ":";
            out += "\n\t-channelId = " + channelId;
            out += "\n\t-offset = " + offset;
            return indent(out, indent);
         }
      }

      public static class MessageIndexOffsets extends KaitaiStructToStringEnabled
      {
         public MessageIndexOffsets(KaitaiStream _io)
         {
            super(_io);
            _read();
         }

         @Override
         private void _read()
         {
            this.entries = new ArrayList<>();
            {
               int i = 0;
               while (!this._io.isEof())
               {
                  this.entries.add(new MessageIndexOffset(this._io));
                  i++;
               }
            }
         }

         private ArrayList<MessageIndexOffset> entries;

         public ArrayList<MessageIndexOffset> entries()
         {
            return entries;
         }

         @Override
         public String toString()
         {
            return toString(0);
         }

         @Override
         public String toString(int indent)
         {
            String out = getClass().getSimpleName() + ":";
            out += "\n\t-entries = " + (entries == null ? "null" : "\n" + EuclidCoreIOTools.getCollectionString("\n", entries, e -> e.toString(indent + 1)));
            return indent(out, indent);
         }
      }

      private Record chunk;

      public Record chunk()
      {
         if (this.chunk != null)
            return this.chunk;
         KaitaiStream io = _root()._io();
         long _pos = io.pos();
         io.seek(ofsChunk());
         this._raw_chunk = io.readBytes(lenChunk());
         KaitaiStream _io__raw_chunk = new ByteBufferKaitaiStream(_raw_chunk);
         this.chunk = new Record(_io__raw_chunk);
         io.seek(_pos);
         return this.chunk;
      }

      private long messageStartTime;
      private long messageEndTime;
      private long ofsChunk;
      private long lenChunk;
      private long lenMessageIndexOffsets;
      private MessageIndexOffsets messageIndexOffsets;
      private long messageIndexLength;
      private PrefixedStr compression;
      private long compressedSize;
      private long uncompressedSize;
      private byte[] _raw_messageIndexOffsets;
      private byte[] _raw_chunk;

      public long messageStartTime()
      {
         return messageStartTime;
      }

      public long messageEndTime()
      {
         return messageEndTime;
      }

      public long ofsChunk()
      {
         return ofsChunk;
      }

      public long lenChunk()
      {
         return lenChunk;
      }

      public long lenMessageIndexOffsets()
      {
         return lenMessageIndexOffsets;
      }

      public MessageIndexOffsets messageIndexOffsets()
      {
         return messageIndexOffsets;
      }

      public long messageIndexLength()
      {
         return messageIndexLength;
      }

      public PrefixedStr compression()
      {
         return compression;
      }

      public long compressedSize()
      {
         return compressedSize;
      }

      public long uncompressedSize()
      {
         return uncompressedSize;
      }

      public byte[] _raw_messageIndexOffsets()
      {
         return _raw_messageIndexOffsets;
      }

      public byte[] _raw_chunk()
      {
         return _raw_chunk;
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      @Override
      public String toString(int indent)
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-messageStartTime = " + messageStartTime;
         out += "\n\t-messageEndTime = " + messageEndTime;
         out += "\n\t-ofsChunk = " + ofsChunk;
         out += "\n\t-lenChunk = " + lenChunk;
         out += "\n\t-lenMessageIndexOffsets = " + lenMessageIndexOffsets;
         out += "\n\t-messageIndexOffsets = " + (messageIndexOffsets == null ? "null" : "\n" + messageIndexOffsets.toString(indent + 1));
         out += "\n\t-messageIndexLength = " + messageIndexLength;
         out += "\n\t-compression = " + compression;
         out += "\n\t-compressedSize = " + compressedSize;
         out += "\n\t-uncompressedSize = " + uncompressedSize;
         return indent(out, indent);
      }
   }

   private abstract static class KaitaiStructToStringEnabled
   {
      protected final FileChannel _io;
      protected long _pos;
      protected int _length;

      public KaitaiStructToStringEnabled(FileChannel _io, long _pos, int _length)
      {
         this._io = _io;
         this._pos = _pos;
         this._length = _length;
      }

      public abstract void _read() throws IOException;

      @Override
      public abstract String toString();

      public String toString(int indent)
      {
         return indent(toString(), indent);
      }
   }

   private Record footer;

   public Record footer()
   {
      if (this.footer != null)
         return this.footer;
      long _pos = this._io.pos();
      this._io.seek(ofsFooter());
      this._raw_footer = this._io.readBytesFull();
      KaitaiStream _io__raw_footer = new ByteBufferKaitaiStream(_raw_footer);
      this.footer = new Record(_io__raw_footer);
      this._io.seek(_pos);
      return this.footer;
   }

   private Integer ofsFooter;

   public Integer ofsFooter()
   {
      if (this.ofsFooter != null)
         return this.ofsFooter;
      int _tmp = (int) (((((_io().size() - 1) - 8) - 20) - 8));
      this.ofsFooter = _tmp;
      return this.ofsFooter;
   }

   private Magic headerMagic;
   private ArrayList<Record> records;
   private Magic footerMagic;
   private byte[] _raw_footer;

   public Magic headerMagic()
   {
      return headerMagic;
   }

   public ArrayList<Record> records()
   {
      return records;
   }

   public Magic footerMagic()
   {
      return footerMagic;
   }

   public byte[] _raw_footer()
   {
      return _raw_footer;
   }
}
