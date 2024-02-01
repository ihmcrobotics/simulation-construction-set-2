package us.ihmc.scs2.session.mcap;

import com.github.luben.zstd.ZstdDecompressCtx;
import gnu.trove.map.hash.TLongObjectHashMap;
import us.ihmc.euclid.tools.EuclidCoreIOTools;

import java.io.IOException;
import java.io.Serial;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MCAP is a modular container format and logging library for pub/sub messages with arbitrary
 * message serialization. It is primarily intended for use in robotics applications, and works well
 * under various workloads, resource constraints, and durability requirements. Time values
 * (`log_time`, `publish_time`, `create_time`) are represented in nanoseconds since a
 * user-understood epoch (i.e. Unix epoch, robot boot time, etc.)
 *
 * @see <a href="https://github.com/foxglove/mcap/tree/c1cc51d/docs/specification#readme">Source</a>
 */
public class MCAP
{
   /**
    * Stream object that this MCAP was parsed from.
    */
   protected FileChannel fileChannel;

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

      private static final TLongObjectHashMap<Opcode> byId = new TLongObjectHashMap<>(15);

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

   private final Magic headerMagic;
   private final List<Record> records;
   private final Magic footerMagic;

   private Record footer;

   public MCAP(FileChannel fileChannel) throws IOException
   {
      this.fileChannel = fileChannel;

      long currentPos = 0;
      headerMagic = new Magic(fileChannel, currentPos);
      currentPos += headerMagic.getItemTotalLength();
      records = new ArrayList<>();
      Record lastRecord;

      do
      {
         lastRecord = new Record(fileChannel, currentPos);
         currentPos += lastRecord.getItemTotalLength();
         records.add(lastRecord);
      }
      while (!(lastRecord.op() == Opcode.FOOTER));

      footerMagic = new Magic(fileChannel, currentPos);
   }

   public FileChannel getFileChannel()
   {
      return fileChannel;
   }

   public Magic headerMagic()
   {
      return headerMagic;
   }

   public List<Record> records()
   {
      return records;
   }

   public Magic footerMagic()
   {
      return footerMagic;
   }

   public Record footer() throws IOException
   {
      if (footer == null)
      {
         footer = new Record(fileChannel, computeOffsetFooter(fileChannel));
      }
      return footer;
   }

   public static class Chunk implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      /**
       * Earliest message log_time in the chunk. Zero if the chunk has no messages.
       */
      private final long messageStartTime;
      /**
       * Latest message log_time in the chunk. Zero if the chunk has no messages.
       */
      private final long messageEndTime;
      /**
       * Uncompressed size of the records field.
       */
      private final long uncompressedSize;
      /**
       * CRC32 checksum of uncompressed records field. A value of zero indicates that CRC validation
       * should not be performed.
       */
      private final long uncompressedCrc32;
      /**
       * compression algorithm. i.e. zstd, lz4, "". An empty string indicates no compression. Refer to
       * well-known compression formats.
       */
      private final String compression;
      /**
       * Offset position of the records in either in the {@code  ByteBuffer} or {@code FileChannel},
       * depending on how this chunk was created.
       */
      private final long offsetRecords;
      /**
       * Length of the records in bytes.
       */
      private final long lengthRecords;
      /**
       * The decompressed records.
       */
      private WeakReference<Records> recordsRef;

      public Chunk(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         int bufferStart = buffer.position();
         messageStartTime = buffer.getLong();
         messageEndTime = buffer.getLong();
         uncompressedSize = buffer.getLong();
         uncompressedCrc32 = Integer.toUnsignedLong(buffer.getInt());
         compression = parseString(buffer);
         lengthRecords = buffer.getLong();
         offsetRecords = _pos + buffer.position() - bufferStart;
      }

      @Override
      public int getItemTotalLength()
      {
         return 3 * Long.BYTES + 2 * Integer.BYTES + compression.length() + Long.BYTES + (int) lengthRecords;
      }

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

      public String compression()
      {
         return compression;
      }

      public long lenRecords()
      {
         return lengthRecords;
      }

      public Records records() throws IOException
      {
         Records records = recordsRef == null ? null : recordsRef.get();

         if (records != null)
            return records;

         if (compression.equalsIgnoreCase(""))
         {
            if (buffer != null)
               records = new Records(buffer, (int) offsetRecords, (int) lengthRecords);
            else
               records = new Records(fileChannel, offsetRecords, (int) lengthRecords);
         }
         else if (compression.equalsIgnoreCase("lz4"))
         {
            LZ4FrameDecoder lz4FrameDecoder = new LZ4FrameDecoder();
            ByteBuffer decompressedData = ByteBuffer.allocate((int) uncompressedSize);

            if (buffer != null)
            {
               lz4FrameDecoder.decode(buffer, (int) offsetRecords, (int) lengthRecords, decompressedData, 0);
            }
            else
            {
               ByteBuffer compressedBuffer = newByteBuffer((int) lengthRecords, false);
               readIntoBuffer(fileChannel, offsetRecords, compressedBuffer);
               lz4FrameDecoder.decode(compressedBuffer, 0, (int) lengthRecords, decompressedData, 0);
            }
            records = new Records(decompressedData);
         }
         else if (compression.equalsIgnoreCase("zstd"))
         {
            try (ZstdDecompressCtx zstdDecompressCtx = new ZstdDecompressCtx())
            {
               ByteBuffer decompressedData;
               if (buffer != null)
               {
                  int previousPosition = buffer.position();
                  int previousLimit = buffer.limit();
                  buffer.limit((int) (offsetRecords + lengthRecords));
                  buffer.position((int) offsetRecords);
                  decompressedData = zstdDecompressCtx.decompress(buffer, (int) uncompressedSize);
                  buffer.position(previousPosition);
                  buffer.limit(previousLimit);
               }
               else
               {
                  ByteBuffer compressedBuffer = newByteBuffer((int) lengthRecords, true);
                  readIntoBuffer(fileChannel, offsetRecords, compressedBuffer);
                  decompressedData = zstdDecompressCtx.decompress(compressedBuffer, (int) uncompressedSize);
               }

               records = new Records(decompressedData);
            }
         }
         else
         {
            throw new UnsupportedOperationException("Unsupported compression algorithm: " + compression);
         }

         recordsRef = new WeakReference<>(records);
         return records;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-messageStartTime = " + messageStartTime;
         out += "\n\t-messageEndTime = " + messageEndTime;
         out += "\n\t-compression = " + compression;
         out += "\n\t-compressedSize = " + lengthRecords;
         out += "\n\t-uncompressedSize = " + uncompressedSize;
         out += "\n\t-uncompressedCrc32 = " + uncompressedCrc32;
         return out;
      }
   }

   public static class DataEnd implements MCAPElement
   {
      private final long dataSectionCrc32;

      public DataEnd(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         dataSectionCrc32 = Integer.toUnsignedLong(buffer.getInt());
      }

      @Override
      public int getItemTotalLength()
      {
         return Integer.BYTES;
      }

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

   public static class Channel implements MCAPElement
   {
      private final int id;
      private final int schemaId;
      private final String topic;
      private final String messageEncoding;
      private final List<TupleStrStr> metadata;
      private final int metadataLength;

      public Channel(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         id = Short.toUnsignedInt(buffer.getShort());
         schemaId = Short.toUnsignedInt(buffer.getShort());
         topic = parseString(buffer);
         messageEncoding = parseString(buffer);
         int start = buffer.position();
         metadata = parseList(buffer, TupleStrStr::new); // TODO Should be able to postpone parsing this.
         int end = buffer.position();
         metadataLength = end - start;
      }

      @Override
      public int getItemTotalLength()
      {
         return 2 * Short.BYTES + 2 * Integer.BYTES + topic.length() + messageEncoding.length() + metadataLength;
      }

      public int id()
      {
         return id;
      }

      public int schemaId()
      {
         return schemaId;
      }

      public String topic()
      {
         return topic;
      }

      public String messageEncoding()
      {
         return messageEncoding;
      }

      public List<TupleStrStr> metadata()
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

   public static class MessageIndex implements MCAPElement
   {
      private final int channelId;
      private final List<MessageIndexEntry> messageIndexEntries;
      private final int messageIndexEntriesLength;

      public MessageIndex(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         int start = buffer.position();
         channelId = Short.toUnsignedInt(buffer.getShort());
         messageIndexEntries = parseList(buffer, MessageIndexEntry::new); // TODO Should be able to postpone parsing this.
         messageIndexEntriesLength = buffer.position() - start;
      }

      @Override
      public int getItemTotalLength()
      {
         return Short.BYTES + messageIndexEntriesLength;
      }

      public static class MessageIndexEntry implements MCAPElement
      {
         /**
          * Time at which the message was recorded.
          */
         private final long logTime;

         /**
          * Offset is relative to the start of the uncompressed chunk data.
          */
         private final long offset;

         public MessageIndexEntry(ByteBuffer buffer)
         {
            logTime = buffer.getLong();
            offset = buffer.getLong();
         }

         @Override
         public int getItemTotalLength()
         {
            return 2 * Long.BYTES;
         }

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

      public int channelId()
      {
         return channelId;
      }

      public List<MessageIndexEntry> records()
      {
         return messageIndexEntries;
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
         out += "\n\t-messageIndexEntries = " + (messageIndexEntries == null ?
               "null" :
               "\n" + EuclidCoreIOTools.getCollectionString("\n", messageIndexEntries, e -> e.toString(indent + 1)));
         return indent(out, indent);
      }
   }

   public static class Statistics implements MCAPElement
   {
      private final long messageCount;
      private final int schemaCount;
      private final long channelCount;
      private final long attachmentCount;
      private final long metadataCount;
      private final long chunkCount;
      private final long messageStartTime;
      private final long messageEndTime;
      private final List<ChannelMessageCount> channelMessageCounts;
      private final int channelMessageCountsLength;

      public Statistics(FileChannel fileChannel, ByteBuffer buffer, long _pos, int length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         int start = buffer.position();
         messageCount = buffer.getLong();
         schemaCount = Short.toUnsignedInt(buffer.getShort());
         channelCount = Integer.toUnsignedLong(buffer.getInt());
         attachmentCount = Integer.toUnsignedLong(buffer.getInt());
         metadataCount = Integer.toUnsignedLong(buffer.getInt());
         chunkCount = Integer.toUnsignedLong(buffer.getInt());
         messageStartTime = buffer.getLong();
         messageEndTime = buffer.getLong();
         channelMessageCounts = parseList(buffer, ChannelMessageCount::new); // TODO Should be able to postpone parsing this.
         channelMessageCountsLength = buffer.position() - start;
      }

      @Override
      public int getItemTotalLength()
      {
         return 3 * Long.BYTES + 5 * Integer.BYTES + Short.BYTES + channelMessageCountsLength;
      }

      public static class ChannelMessageCount implements MCAPElement
      {
         private final int channelId;
         private final long messageCount;

         public ChannelMessageCount(ByteBuffer buffer)
         {
            channelId = Short.toUnsignedInt(buffer.getShort());
            messageCount = buffer.getLong();
         }

         @Override
         public int getItemTotalLength()
         {
            return Short.BYTES + Long.BYTES;
         }

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

      public List<ChannelMessageCount> channelMessageCounts()
      {
         return channelMessageCounts;
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
         out += "\n\t-channelMessageCounts = \n" + EuclidCoreIOTools.getCollectionString("\n", channelMessageCounts, e -> e.toString(1));
         return out;
      }
   }

   public static class AttachmentIndex implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private final long offsetAttachment;
      private final long lengthAttachment;
      private final long logTime;
      private final long createTime;
      private final long dataSize;
      private final String name;
      private final String mediaType;

      private WeakReference<Record> attachmentRef;

      private AttachmentIndex(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         offsetAttachment = buffer.getLong();
         lengthAttachment = buffer.getLong();
         logTime = buffer.getLong();
         createTime = buffer.getLong();
         dataSize = buffer.getLong();
         name = parseString(buffer);
         mediaType = parseString(buffer);
      }

      @Override
      public int getItemTotalLength()
      {
         return 5 * Long.BYTES + 2 * Integer.BYTES + name.length() + mediaType.length();
      }

      public Record attachment()
      {
         Record attachment = attachmentRef == null ? null : attachmentRef.get();

         if (attachment == null)
         {
            // TODO Check if we can use the lenAttachment for verification or something.
            if (buffer != null)
               attachment = new Record(buffer.position((int) offsetAttachment));
            else
               attachment = new Record(fileChannel, offsetAttachment);
            attachmentRef = new WeakReference<>(attachment);
         }

         return attachment;
      }

      public long offsetAttachment()
      {
         return offsetAttachment;
      }

      public long lengthAttachment()
      {
         return lengthAttachment;
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

      public String name()
      {
         return name;
      }

      public String mediaType()
      {
         return mediaType;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-ofsAttachment = " + offsetAttachment;
         out += "\n\t-lenAttachment = " + lengthAttachment;
         out += "\n\t-logTime = " + logTime;
         out += "\n\t-createTime = " + createTime;
         out += "\n\t-dataSize = " + dataSize;
         out += "\n\t-name = " + name;
         out += "\n\t-mediaType = " + mediaType;
         return out;
      }
   }

   public static class Schema implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private final int id;
      private final String name;
      private final String encoding;
      private final long lengthData;
      private final long offsetData;
      private WeakReference<ByteBuffer> dataRef;

      public Schema(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         id = Short.toUnsignedInt(buffer.getShort());
         name = parseString(buffer);
         encoding = parseString(buffer);
         lengthData = Integer.toUnsignedLong(buffer.getInt());
         offsetData = buffer.position();
         buffer.position((int) (offsetData + lengthData)); // Skip the data
      }

      @Override
      public int getItemTotalLength()
      {
         return Short.BYTES + 3 * Integer.BYTES + name.length() + encoding.length() + (int) lengthData;
      }

      public int id()
      {
         return id;
      }

      public String name()
      {
         return name;
      }

      public String encoding()
      {
         return encoding;
      }

      public ByteBuffer data()
      {
         ByteBuffer data = this.dataRef == null ? null : this.dataRef.get();

         if (data == null)
         {
            data = newByteBuffer((int) lengthData, false);
            if (buffer != null)
               data.put(0, buffer, (int) offsetData, (int) lengthData);
            else
               readIntoBuffer(fileChannel, offsetData, data);
            dataRef = new WeakReference<>(data);
         }
         return data;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ":";
         out += "\n\t-id = " + id;
         out += "\n\t-name = " + name;
         out += "\n\t-encoding = " + encoding;
         out += "\n\t-lengthData = " + lengthData;
         out += "\n\t-data = " + Arrays.toString(data().array());
         return out;
      }
   }

   public static class SummaryOffset implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private final Opcode groupOpcode;
      private final long offsetGroup;
      private final long lengthGroup;

      private WeakReference<Records> groupRef;

      public SummaryOffset(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         groupOpcode = Opcode.byId(Byte.toUnsignedInt(buffer.get()));
         offsetGroup = buffer.getLong();
         lengthGroup = buffer.getLong();
      }

      @Override
      public int getItemTotalLength()
      {
         return Byte.BYTES + 2 * Long.BYTES;
      }

      public Records group() throws IOException
      {
         Records group = groupRef == null ? null : groupRef.get();

         if (group == null)
         {
            if (buffer != null)
               group = new Records(buffer.position((int) offsetGroup));
            else
               group = new Records(fileChannel, offsetGroup, (int) lengthGroup);
            groupRef = new WeakReference<>(group);
         }
         return group;
      }

      public Opcode groupOpcode()
      {
         return groupOpcode;
      }

      public long offsetGroup()
      {
         return offsetGroup;
      }

      public long lengthGroup()
      {
         return lengthGroup;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-groupOpcode = " + groupOpcode;
         out += "\n\t-offsetGroup = " + offsetGroup;
         out += "\n\t-lengthGroup = " + lengthGroup;
         return out;
      }
   }

   public static class Attachment implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private final long logTime;
      private final long createTime;
      private final String name;
      private final String mediaType;
      private final long lengthData;
      private final long offsetData;
      private WeakReference<ByteBuffer> dataRef;
      private final long crc32;
      private final long crc32InputStart;
      private final int crc32InputLength;
      private WeakReference<ByteBuffer> crc32InputRef;

      private Attachment(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         int bufferStart = buffer.position();
         crc32InputStart = _pos;
         logTime = buffer.getLong();
         createTime = buffer.getLong();
         name = parseString(buffer);
         mediaType = parseString(buffer);
         lengthData = buffer.getLong();
         offsetData = _pos + buffer.position() - bufferStart;
         buffer.position((int) (buffer.position() + lengthData));
         crc32InputLength = buffer.position() - bufferStart;
         crc32 = Integer.toUnsignedLong(buffer.getInt());
      }

      @Override
      public int getItemTotalLength()
      {
         return 3 * Long.BYTES + 3 * Integer.BYTES + name.length() + mediaType.length() + (int) lengthData;
      }

      public ByteBuffer crc32Input()
      {
         ByteBuffer crc32Input = this.crc32InputRef == null ? null : this.crc32InputRef.get();

         if (crc32Input == null)
         {
            crc32Input = newByteBuffer(crc32InputLength, false);
            if (buffer != null)
               crc32Input.put(0, buffer, (int) crc32InputStart, crc32InputLength);
            else
               readIntoBuffer(fileChannel, crc32InputStart, crc32Input);
            crc32InputRef = new WeakReference<>(crc32Input);
         }

         return crc32Input;
      }

      public long logTime()
      {
         return logTime;
      }

      public long createTime()
      {
         return createTime;
      }

      public String name()
      {
         return name;
      }

      public String mediaType()
      {
         return mediaType;
      }

      public long lenData()
      {
         return lengthData;
      }

      public ByteBuffer data()
      {
         ByteBuffer data = this.dataRef == null ? null : this.dataRef.get();

         if (data == null)
         {
            data = newByteBuffer((int) lengthData, false);
            if (buffer != null)
               data.put(0, buffer, (int) offsetData, (int) lengthData);
            else
               readIntoBuffer(fileChannel, offsetData, data);
            dataRef = new WeakReference<>(data);
         }
         return data;
      }

      public void unloadData()
      {
         dataRef = null;
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
         out += "\n\t-lengthData = " + lengthData;
         //         out += "\n\t-data = " + data;
         out += "\n\t-crc32 = " + crc32;
         return out;
      }
   }

   public static class Metadata implements MCAPElement
   {
      private final String name;
      private final List<TupleStrStr> metadata;
      private final int metadataLength;

      private Metadata(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         name = parseString(buffer);
         int start = buffer.position();
         metadata = parseList(buffer, TupleStrStr::new); // TODO Looks into postponing the loading of the metadata.
         metadataLength = buffer.position() - start;
      }

      @Override
      public int getItemTotalLength()
      {
         return Integer.BYTES + name.length() + metadataLength;
      }

      public String name()
      {
         return name;
      }

      public List<TupleStrStr> metadata()
      {
         return metadata;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-name = " + name;
         out += "\n\t-metadata = " + EuclidCoreIOTools.getCollectionString(", ", metadata, e -> e.key());
         return out;
      }
   }

   public static class Header implements MCAPElement
   {
      private final String profile;
      private final String library;

      public Header(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         profile = parseString(buffer);
         library = parseString(buffer);
      }

      @Override
      public int getItemTotalLength()
      {
         return 2 * Integer.BYTES + profile.length() + library.length();
      }

      public String profile()
      {
         return profile;
      }

      public String library()
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

   public static class Message implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private int channelId;
      private long sequence;
      private long logTime;
      private long publishTime;
      private long offsetData;
      private int lengthData;
      private WeakReference<ByteBuffer> messageBufferRef;
      private WeakReference<byte[]> messageDataRef;

      public static Message createSpoofMessageForTesting(int channelId, byte[] data)
      {
         return new Message()
         {
            @Override
            public int channelId()
            {
               return channelId;
            }

            @Override
            public byte[] messageData()
            {
               return data;
            }
         };
      }

      private Message()
      {
         fileChannel = null;
         buffer = null;
      }

      public Message(ByteBuffer buffer, long _pos, int _length)
      {
         this(null, buffer, _pos, _length);
      }

      public Message(FileChannel fileChannel, long _pos, int _length)
      {
         this(fileChannel, null, _pos, _length);
      }

      private Message(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
            messageBufferRef = new WeakReference<>(buffer);
         }

         int bufferStart = buffer.position();
         channelId = Short.toUnsignedInt(buffer.getShort());
         sequence = Integer.toUnsignedLong(buffer.getInt());
         logTime = buffer.getLong();
         publishTime = buffer.getLong();
         offsetData = _pos + buffer.position() - bufferStart;
         lengthData = _length - (Short.BYTES + Integer.BYTES + 2 * Long.BYTES);
      }

      @Override
      public int getItemTotalLength()
      {
         return lengthData + Short.BYTES + Integer.BYTES + 2 * Long.BYTES;
      }

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

      /**
       * Returns the offset of the data portion of this message in the buffer returned by
       * {@link #messageBuffer()}.
       *
       * @return the offset of the data portion of this message.
       */
      public int offsetData()
      {
         return (int) offsetData;
      }

      /**
       * Returns the length of the data portion of this message.
       *
       * @return the length of the data portion of this message.
       */
      public int lengthData()
      {
         return lengthData;
      }

      /**
       * Returns the buffer containing this message, the data AND the header. Use {@link #offsetData()}
       * and {@link #lengthData()} to get the data portion.
       *
       * @return the buffer containing this message.
       */
      public ByteBuffer messageBuffer()
      {
         ByteBuffer messageBuffer = messageBufferRef == null ? null : messageBufferRef.get();
         if (messageBuffer == null)
         { // That means that fileChannel is not null, otherwise the reference to the buffer is maintained so the messageBuffer should not have been GC'ed.
            messageBuffer = newByteBuffer(lengthData, false);
            readIntoBuffer(fileChannel, offsetData, messageBuffer);
            messageBufferRef = new WeakReference<>(messageBuffer);
         }
         return messageBuffer;
      }

      public byte[] messageData()
      {
         byte[] messageData = messageDataRef == null ? null : messageDataRef.get();

         if (messageData == null)
         {
            messageData = new byte[lengthData];
            messageBuffer().get(offsetData(), messageData);
            messageDataRef = new WeakReference<>(messageData);
         }
         return messageData;
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

   public static class TupleStrStr implements MCAPElement
   {
      private final String key;
      private final String value;

      public TupleStrStr(ByteBuffer buffer)
      {
         key = parseString(buffer);
         value = parseString(buffer);
      }

      @Override
      public int getItemTotalLength()
      {
         return key.length() + value.length() + 2 * Integer.BYTES;
      }

      public String key()
      {
         return key;
      }

      public String value()
      {
         return value;
      }

      @Override
      public String toString()
      {
         return (key + ": " + value).replace("\n", "");
      }
   }

   public static class MetadataIndex implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      private final long offsetMetadata;
      private final long lengthMetadata;
      private final String name;
      private WeakReference<Record> metadataRef;

      private MetadataIndex(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         offsetMetadata = buffer.getLong();
         lengthMetadata = buffer.getLong();
         name = parseString(buffer);
      }

      @Override
      public int getItemTotalLength()
      {
         return 2 * Long.BYTES + Integer.BYTES + name.length();
      }

      public Record metadata()
      {
         Record metadata = metadataRef == null ? null : metadataRef.get();

         if (metadata == null)
         {
            if (fileChannel != null)
            {
               metadata = new Record(fileChannel, offsetMetadata);
            }
            else
            {
               buffer.position((int) offsetMetadata);
               metadata = new Record(buffer);
            }
            metadataRef = new WeakReference<>(metadata);
         }
         return metadata;
      }

      public long offsetMetadata()
      {
         return offsetMetadata;
      }

      public long lengthMetadata()
      {
         return lengthMetadata;
      }

      public String name()
      {
         return name;
      }

      @Override
      public String toString()
      {
         String out = getClass().getSimpleName() + ": ";
         out += "\n\t-offsetMetadata = " + offsetMetadata;
         out += "\n\t-lengthMetadata = " + lengthMetadata;
         out += "\n\t-name = " + name;
         return out;
      }
   }

   public static class Magic implements MCAPElement
   {
      public static final int MAGIC_SIZE = 8;
      public static final byte[] MAGIC_BYTES = {-119, 77, 67, 65, 80, 48, 13, 10};

      private final byte[] magic;

      public Magic(FileChannel fileChannel, long _pos)
      {
         ByteBuffer buffer = newByteBuffer(MAGIC_SIZE, false);
         readIntoBuffer(fileChannel, _pos, buffer);
         magic = new byte[MAGIC_SIZE];
         buffer.get(magic);
         if (!(Arrays.equals(magic, MAGIC_BYTES)))
            throw new ValidationNotEqualError(MAGIC_BYTES, magic, buffer);
      }

      @Override
      public int getItemTotalLength()
      {
         return MAGIC_SIZE;
      }

      public byte[] magic()
      {
         return magic;
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
         out += "\n\t-magic = " + Arrays.toString(magic);
         return indent(out, indent);
      }
   }

   public static class Records extends ArrayList<Record>
   {
      public Records(ByteBuffer buffer)
      {
         this(buffer, buffer.position(), buffer.remaining());
      }

      public Records(ByteBuffer buffer, long _pos, int _length)
      {
         parseList(buffer, Record::new, _pos, _length, this);
      }

      public Records(FileChannel fileChannel, long _pos, int _length)
      {
         parseList(fileChannel, Record::new, _pos, _length, this);
      }

      @Override
      public String toString()
      {
         return toString(0);
      }

      public String toString(int indent)
      {
         if (isEmpty())
            return indent(getClass().getSimpleName() + ": []", indent);

         String out = getClass().getSimpleName() + "[\n";
         out += EuclidCoreIOTools.getCollectionString("\n", this, r -> r.toString(indent + 1));
         return indent(out, indent);
      }
   }

   public static class Footer implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final long ofsSummarySection;
      private final long ofsSummaryOffsetSection;
      private final long summaryCrc32;
      private Integer ofsSummaryCrc32Input;
      private Records summaryOffsetSection;
      private Records summarySection;

      public Footer(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         ofsSummarySection = buffer.getLong();
         ofsSummaryOffsetSection = buffer.getLong();
         summaryCrc32 = Integer.toUnsignedLong(buffer.getInt());
      }

      @Override
      public int getItemTotalLength()
      {
         return 2 * Long.BYTES + Integer.BYTES;
      }

      public Records summarySection() throws IOException
      {
         if (summarySection == null && ofsSummarySection != 0)
         {
            long length = ((ofsSummaryOffsetSection != 0 ? ofsSummaryOffsetSection : computeOffsetFooter(fileChannel)) - ofsSummarySection);
            summarySection = new Records(fileChannel, ofsSummarySection, (int) length);
         }
         return summarySection;
      }

      public Records summaryOffsetSection() throws IOException
      {
         if (summaryOffsetSection == null && ofsSummaryOffsetSection != 0)
         {
            summaryOffsetSection = new Records(fileChannel, ofsSummaryOffsetSection, (int) (computeOffsetFooter(fileChannel) - ofsSummaryOffsetSection));
         }
         return summaryOffsetSection;
      }

      public Integer ofsSummaryCrc32Input() throws IOException
      {
         if (ofsSummaryCrc32Input == null)
         {
            ofsSummaryCrc32Input = (int) ((ofsSummarySection() != 0 ? ofsSummarySection() : computeOffsetFooter(fileChannel)));
         }
         return ofsSummaryCrc32Input;
      }

      private byte[] summaryCrc32Input;

      public byte[] summaryCrc32Input() throws IOException
      {
         if (summaryCrc32Input == null)
         {
            ByteBuffer tmpBuffer = ByteBuffer.allocate((int) (fileChannel.size() - ofsSummaryCrc32Input() - 8 - 4));
            fileChannel.read(tmpBuffer, ofsSummaryCrc32Input());
            summaryCrc32Input = tmpBuffer.array();
         }
         return summaryCrc32Input;
      }

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

   public static class Record implements MCAPElement
   {
      public static final int RECORD_HEADER_LENGTH = 9;

      private final FileChannel fileChannel;
      private final ByteBuffer buffer;

      private final Opcode op;
      private final long lengthBody;
      private final long bodyPos;
      private WeakReference<Object> bodyRef;

      public Record(ByteBuffer buffer)
      {
         this.buffer = buffer;
         this.fileChannel = null;

         op = Opcode.byId(Byte.toUnsignedInt(buffer.get()));
         lengthBody = buffer.getLong();
         bodyPos = buffer.position();
      }

      public Record(FileChannel fileChannel, long _pos)
      {
         this.fileChannel = fileChannel;
         this.buffer = null;

         ByteBuffer buffer = newByteBuffer(RECORD_HEADER_LENGTH, false);
         readIntoBuffer(fileChannel, _pos, buffer);
         op = Opcode.byId(Byte.toUnsignedInt(buffer.get()));
         lengthBody = buffer.getLong();
         bodyPos = _pos + RECORD_HEADER_LENGTH;
      }

      private Object readBody()
      {
         Object body = bodyRef == null ? null : bodyRef.get();

         if (body != null)
            return body;

         if (op == null)
         {
            if (fileChannel != null)
            {
               ByteBuffer bb = newByteBuffer((int) lengthBody, false);
               readIntoBuffer(fileChannel, bodyPos, bb);
               body = bb.array();
            }
            else
            {
               body = new byte[(int) lengthBody];
               buffer.get((int) bodyPos, (byte[]) body);
            }
            return body;
         }

         body = switch (op)
         {
            case MESSAGE -> new Message(fileChannel, buffer, bodyPos, (int) lengthBody);
            case METADATA_INDEX -> new MetadataIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHUNK -> new Chunk(fileChannel, buffer, bodyPos, (int) lengthBody);
            case SCHEMA -> new Schema(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHUNK_INDEX -> new ChunkIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case DATA_END -> new DataEnd(fileChannel, buffer, bodyPos, (int) lengthBody);
            case ATTACHMENT_INDEX -> new AttachmentIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case STATISTICS -> new Statistics(fileChannel, buffer, bodyPos, (int) lengthBody);
            case MESSAGE_INDEX -> new MessageIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHANNEL -> new Channel(fileChannel, buffer, bodyPos, (int) lengthBody);
            case METADATA -> new Metadata(fileChannel, buffer, bodyPos, (int) lengthBody);
            case ATTACHMENT -> new Attachment(fileChannel, buffer, bodyPos, (int) lengthBody);
            case HEADER -> new Header(fileChannel, buffer, bodyPos, (int) lengthBody);
            case FOOTER -> new Footer(fileChannel, buffer, bodyPos, (int) lengthBody);
            case SUMMARY_OFFSET -> new SummaryOffset(fileChannel, buffer, bodyPos, (int) lengthBody);
         };

         bodyRef = new WeakReference<>(body);
         return body;
      }

      public Opcode op()
      {
         return op;
      }

      public long lengthBody()
      {
         return lengthBody;
      }

      public Object body()
      {
         Object body = bodyRef == null ? null : bodyRef.get();

         if (body != null)
            return body;

         if (op == null)
         {
            if (fileChannel != null)
            {
               ByteBuffer bb = newByteBuffer((int) lengthBody, false);
               readIntoBuffer(fileChannel, bodyPos, bb);
               body = bb.array();
            }
            else
            {
               body = new byte[(int) lengthBody];
               buffer.get((int) bodyPos, (byte[]) body);
            }
            return body;
         }

         body = switch (op)
         {
            case MESSAGE -> new Message(fileChannel, buffer, bodyPos, (int) lengthBody);
            case METADATA_INDEX -> new MetadataIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHUNK -> new Chunk(fileChannel, buffer, bodyPos, (int) lengthBody);
            case SCHEMA -> new Schema(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHUNK_INDEX -> new ChunkIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case DATA_END -> new DataEnd(fileChannel, buffer, bodyPos, (int) lengthBody);
            case ATTACHMENT_INDEX -> new AttachmentIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case STATISTICS -> new Statistics(fileChannel, buffer, bodyPos, (int) lengthBody);
            case MESSAGE_INDEX -> new MessageIndex(fileChannel, buffer, bodyPos, (int) lengthBody);
            case CHANNEL -> new Channel(fileChannel, buffer, bodyPos, (int) lengthBody);
            case METADATA -> new Metadata(fileChannel, buffer, bodyPos, (int) lengthBody);
            case ATTACHMENT -> new Attachment(fileChannel, buffer, bodyPos, (int) lengthBody);
            case HEADER -> new Header(fileChannel, buffer, bodyPos, (int) lengthBody);
            case FOOTER -> new Footer(fileChannel, buffer, bodyPos, (int) lengthBody);
            case SUMMARY_OFFSET -> new SummaryOffset(fileChannel, buffer, bodyPos, (int) lengthBody);
         };

         bodyRef = new WeakReference<>(body);
         return body;
      }

      @Override
      public int getItemTotalLength()
      {
         return (int) (RECORD_HEADER_LENGTH + lengthBody);
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
         out += "\n\t-lengthBody = " + lengthBody;
         Object body = body();
         out += "\n\t-body = " + (body == null ? "null" : "\n" + ((MCAPElement) body).toString(indent + 2));
         return indent(out, indent);
      }
   }

   public static class ChunkIndex implements MCAPElement
   {
      private final FileChannel fileChannel;
      private final ByteBuffer buffer;
      /**
       * Earliest message log_time in the chunk. Zero if the chunk has no messages.
       */
      private final long messageStartTime;
      /**
       * Latest message log_time in the chunk. Zero if the chunk has no messages.
       */
      private final long messageEndTime;
      /**
       * Offset to the chunk record from the start of the file.
       */
      private final long ofsChunk;
      /**
       * Byte length of the chunk record, including opcode and length prefix.
       */
      private final long lenChunk;
      /**
       * Total length in bytes of the message index records after the chunk.
       */
      private final long lenMessageIndexOffsets;
      /**
       * Mapping from channel ID to the offset of the message index record for that channel after the
       * chunk, from the start of the file. An empty map indicates no message indexing is available.
       */
      private final MessageIndexOffsets messageIndexOffsets;
      /**
       * Total length in bytes of the message index records after the chunk.
       */
      private final long messageIndexLength;
      /**
       * The compression used within the chunk. Refer to well-known compression formats. This field should
       * match the the value in the corresponding Chunk record.
       */
      private final String compression;
      /**
       * The size of the chunk records field.
       */
      private final long compressedSize;
      /**
       * The uncompressed size of the chunk records field. This field should match the value in the
       * corresponding Chunk record.
       */
      private final long uncompressedSize;

      private ChunkIndex(FileChannel fileChannel, ByteBuffer buffer, long _pos, int _length)
      {
         this.fileChannel = fileChannel;
         this.buffer = buffer;

         if (buffer == null)
         {
            buffer = newByteBuffer(_length, false);
            readIntoBuffer(fileChannel, _pos, buffer);
         }
         else
         {
            buffer.position((int) _pos);
         }

         messageStartTime = buffer.getLong();
         messageEndTime = buffer.getLong();
         ofsChunk = buffer.getLong();
         lenChunk = buffer.getLong();
         lenMessageIndexOffsets = Integer.toUnsignedLong(buffer.getInt());
         messageIndexOffsets = new MessageIndexOffsets(buffer,
                                                       (int) lenMessageIndexOffsets); // TODO Looks into postponing the loading of the messageIndexOffsets.
         messageIndexLength = buffer.getLong();
         compression = parseString(buffer);
         compressedSize = buffer.getLong();
         uncompressedSize = buffer.getLong();
      }

      @Override
      public int getItemTotalLength()
      {
         return 7 * Long.BYTES + 2 * Integer.BYTES + messageIndexOffsets.getItemTotalLength() + compression.length();
      }

      public static class MessageIndexOffset implements MCAPElement
      {
         /**
          * Channel ID.
          */
         private final int channelId;
         /**
          * Offset of the message index record for that channel after the chunk, from the start of the file.
          */
         private final long offset;

         public MessageIndexOffset(ByteBuffer buffer)
         {
            channelId = Short.toUnsignedInt(buffer.getShort());
            offset = buffer.getLong();
         }

         @Override
         public int getItemTotalLength()
         {
            return Short.BYTES + Long.BYTES;
         }

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

      public static class MessageIndexOffsets implements MCAPElement
      {
         private final List<MessageIndexOffset> entries;
         private final int entriesLength;

         public MessageIndexOffsets(ByteBuffer buffer, int _length)
         {
            entries = new ArrayList<>();

            int remaining = _length;

            while (remaining > 0)
            {
               MessageIndexOffset entry = new MessageIndexOffset(buffer);
               entries.add(entry);
               remaining -= entry.getItemTotalLength();
            }
            entriesLength = _length;
         }

         @Override
         public int getItemTotalLength()
         {
            return entriesLength;
         }

         public List<MessageIndexOffset> entries()
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

      private WeakReference<Record> chunkRef;

      public Record chunk()
      {
         Record chunk = chunkRef == null ? null : chunkRef.get();

         if (chunk == null)
         {
            // TODO Check if we can use the lenChunk for verification or something.
            if (buffer != null)
               chunk = new Record(buffer.position((int) ofsChunk));
            else
               chunk = new Record(fileChannel, ofsChunk);
            chunkRef = new WeakReference<>(chunk);
         }
         return chunkRef.get();
      }

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

      public String compression()
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

   public interface MCAPElement
   {
      int getItemTotalLength();

      default String toString(int indent)
      {
         return indent(toString(), indent);
      }
   }

   public static int computeOffsetFooter(FileChannel fileChannel) throws IOException
   {
      return (int) (((((fileChannel.size() - 1) - 8) - 20) - 8));
   }

   /**
    * Parses a string from the buffer. The length of the string is read from the buffer as a prefixed 32-bit unsigned integer.
    *
    * @param buffer the buffer to read from.
    * @return the string read from the buffer.
    */
   public static String parseString(ByteBuffer buffer)
   {
      return parseString(buffer, Integer.toUnsignedLong(buffer.getInt()));
   }

   /**
    * Parses a string from the buffer. The length of the string is given as a parameter.
    *
    * @param buffer the buffer to read from.
    * @param length the length in bytes of the string to read.
    * @return the string read from the buffer.
    */
   public static String parseString(ByteBuffer buffer, long length)
   {
      byte[] bytes = new byte[(int) length];
      buffer.get(bytes);
      return new String(bytes, StandardCharsets.UTF_8);
   }

   /**
    * Parses a list from the buffer. The length of the list is read from the buffer as a prefixed 32-bit unsigned integer.
    *
    * @param buffer        the buffer to read from.
    * @param elementParser the function to use to parse each element of the list.
    * @param <T>           the type of the elements in the list.
    * @return the list read from the buffer.
    */
   public static <T extends MCAPElement> List<T> parseList(ByteBuffer buffer, ByteBufferReader<T> elementParser)
   {
      return parseList(buffer, elementParser, Integer.toUnsignedLong(buffer.getInt()));
   }

   /**
    * Parses a list from the buffer. The length of the list is given as a parameter.
    *
    * @param buffer        the buffer to read from.
    * @param elementParser the function to use to parse each element of the list.
    * @param length        the length in bytes of the list to read.
    * @param <T>           the type of the elements in the list.
    * @return the list read from the buffer.
    */
   public static <T extends MCAPElement> List<T> parseList(ByteBuffer buffer, ByteBufferReader<T> elementParser, long length)
   {
      return parseList(buffer, elementParser, buffer.position(), length);
   }

   /**
    * Parses a list from the buffer. The length of the list is given as a parameter.
    *
    * @param buffer        the buffer to read from.
    * @param elementParser the function to use to parse each element of the list.
    * @param offset        the offset in the buffer to start reading from.
    * @param length        the length in bytes of the list to read.
    * @param <T>           the type of the elements in the list.
    * @return the list read from the buffer.
    */
   public static <T extends MCAPElement> List<T> parseList(ByteBuffer buffer, ByteBufferReader<T> elementParser, long offset, long length)
   {
      return parseList(buffer, elementParser, offset, length, null);
   }

   public static <T extends MCAPElement> List<T> parseList(ByteBuffer buffer, ByteBufferReader<T> elementParser, long offset, long length, List<T> listToPack)
   {
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.position((int) offset);
      int position = buffer.position();
      long limit = position + length;
      if (listToPack == null)
         listToPack = new ArrayList<>();

      while (position < limit)
      {
         buffer.position(position);
         T parsed = elementParser.parse(buffer);
         listToPack.add(parsed);
         position += parsed.getItemTotalLength();
      }

      return listToPack;
   }

   public interface ByteBufferReader<T extends MCAPElement>
   {
      T parse(ByteBuffer buffer);
   }

   public static <T extends MCAPElement> List<T> parseList(FileChannel fileChannel, FileChannelReader<T> elementParser, long offset, long length)
   {
      return parseList(fileChannel, elementParser, offset, length, null);
   }

   public static <T extends MCAPElement> List<T> parseList(FileChannel fileChannel,
                                                           FileChannelReader<T> elementParser,
                                                           long offset,
                                                           long length,
                                                           List<T> listToPack)
   {
      long position = offset;
      long limit = position + length;
      if (listToPack == null)
         listToPack = new ArrayList<>();

      while (position < limit)
      {
         T parsed = elementParser.parse(fileChannel, position);
         listToPack.add(parsed);
         position += parsed.getItemTotalLength();
      }

      return listToPack;
   }

   public interface FileChannelReader<T extends MCAPElement>
   {
      T parse(FileChannel fileChannel, long position);
   }

   private static String indent(String stringToIndent, int indent)
   {
      if (indent <= 0)
         return stringToIndent;
      String indentStr = "\t".repeat(indent);
      return indentStr + stringToIndent.replace("\n", "\n" + indentStr);
   }

   private static ByteBuffer newByteBuffer(int length, boolean direct)
   {
      ByteBuffer byteBuffer;
      if (direct)
         byteBuffer = ByteBuffer.allocateDirect(length);
      else
         byteBuffer = ByteBuffer.allocate(length);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      return byteBuffer;
   }

   private static void readIntoBuffer(FileChannel fileChannel, long _pos, ByteBuffer buffer)
   {
      try
      {
         buffer.clear();
         fileChannel.read(buffer, _pos);
         buffer.flip();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Common ancestor for all error originating from Kaitai Struct usage. Stores KSY source path,
    * pointing to an element supposedly guilty of an error.
    */
   public static class MCAPStructError extends RuntimeException
   {
      @Serial
      private static final long serialVersionUID = 3448466497836212719L;

      public MCAPStructError(String msg)
      {
         super(msg);
      }
   }

   /**
    * Common ancestor for all validation failures. Stores pointer to KaitaiStream IO object which was
    * involved in an error.
    */
   public static class ValidationFailedError extends MCAPStructError
   {
      @Serial
      private static final long serialVersionUID = 4069741066320518907L;

      public ValidationFailedError(String msg, ByteBuffer buffer)
      {
         super("at pos " + buffer.position() + ": validation failed: " + msg);
      }

      protected static String byteArrayToHex(byte[] arr)
      {
         StringBuilder sb = new StringBuilder("[");
         for (int i = 0; i < arr.length; i++)
         {
            if (i > 0)
               sb.append(' ');
            sb.append(String.format("%02x", arr[i]));
         }
         sb.append(']');
         return sb.toString();
      }
   }

   /**
    * Signals validation failure: we required "actual" value to be equal to "expected", but it turned
    * out that it's not.
    */
   public static class ValidationNotEqualError extends ValidationFailedError
   {
      @Serial
      private static final long serialVersionUID = -6127683772774212751L;

      public ValidationNotEqualError(byte[] expected, byte[] actual, ByteBuffer io)
      {
         super("not equal, expected " + byteArrayToHex(expected) + ", but got " + byteArrayToHex(actual), io);
      }

      public ValidationNotEqualError(Object expected, Object actual, ByteBuffer io)
      {
         super("not equal, expected " + expected + ", but got " + actual, io);
      }
   }
}