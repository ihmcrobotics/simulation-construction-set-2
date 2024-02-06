package us.ihmc.scs2.session.mcap;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.visual.ColorDefinitions;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinitionFactory;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicGroupDefinition;
import us.ihmc.scs2.session.mcap.MCAPSchema.MCAPSchemaField;
import us.ihmc.yoVariables.euclid.YoPoint3D;
import us.ihmc.yoVariables.euclid.YoPose3D;
import us.ihmc.yoVariables.euclid.YoQuaternion;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePoint3D;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFramePose3D;
import us.ihmc.yoVariables.euclid.referenceFrame.YoFrameQuaternion;
import us.ihmc.yoVariables.registry.YoRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MCAPFrameTransformManager
{
   private static final String FOXGLOVE_PREFIX = "foxglove::FrameTransform.";
   private static final String WORLD_FRAME_NAME = "world";
   private static final String FRAME_FIELD_TYPE = "string";
   private static final String PARENT_FRAME_FIELD_NAME = "parent_frame_id";
   private static final String CHILD_FRAME_FIELD_NAME = "child_frame_id";
   private static final String ROTATION_FIELD_NAME = "rotation";
   private static final String ROTATION_X_FIELD_NAME = "rotation.x";
   private static final String ROTATION_Y_FIELD_NAME = "rotation.y";
   private static final String ROTATION_Z_FIELD_NAME = "rotation.z";
   private static final String ROTATION_W_FIELD_NAME = "rotation.w";
   private static final String TRANSLATION_FIELD_NAME = "translation";
   private static final String TRANSLATION_X_FIELD_NAME = "translation.x";
   private static final String TRANSLATION_Y_FIELD_NAME = "translation.y";
   private static final String TRANSLATION_Z_FIELD_NAME = "translation.z";

   private final YoRegistry registry = new YoRegistry(getClass().getSimpleName());
   private final ReferenceFrame inertialFrame;
   private MCAPSchema foxgloveFrameTransformSchema;
   private final List<YoFoxGloveFrameTransform> transformList = new ArrayList<>();
   private final Map<String, YoFoxGloveFrameTransform> rawNameToTransformMap = new LinkedHashMap<>();
   private final Map<String, YoFoxGloveFrameTransform> sanitizedNameToTransformMap = new LinkedHashMap<>();
   private final TIntHashSet channelIds = new TIntHashSet();
   /**
    * Sometimes, tfs are defined with a parent that doesn't exist, they are not yet attached to world.
    */
   private final Set<String> unattachedRootNames = new LinkedHashSet<>();

   private final YoGraphicGroupDefinition yoGraphicGroupDefinition = new YoGraphicGroupDefinition("FoxgloveFrameTransforms");
   private MCAP.Schema mcapSchema;

   public MCAPFrameTransformManager(ReferenceFrame inertialFrame)
   {
      this.inertialFrame = inertialFrame;
   }

   public void initialize(MCAP mcap) throws IOException
   {
      for (MCAP.Record record : mcap.records())
      {
         if (record.op() != MCAP.Opcode.SCHEMA)
            continue;

         mcapSchema = (MCAP.Schema) record.body();
         if (mcapSchema.name().equalsIgnoreCase("foxglove::FrameTransform"))
         {
            if (mcapSchema.encoding().equalsIgnoreCase("ros2msg"))
            {
               foxgloveFrameTransformSchema = ROS2SchemaParser.loadSchema(mcapSchema);
            }
            else if (mcapSchema.encoding().equalsIgnoreCase("omgidl"))
            {
               foxgloveFrameTransformSchema = OMGIDLSchemaParser.loadSchema(mcapSchema);
            }
            else
            {
               throw new UnsupportedOperationException("Unsupported encoding: " + mcapSchema.encoding());
            }
            break;
         }
      }

      if (foxgloveFrameTransformSchema == null)
      {
         LogTools.error("Could not find the schema for foxglove::FrameTransform");
         return;
      }

      // Flatten the schema to make it easier to read.
      foxgloveFrameTransformSchema = foxgloveFrameTransformSchema.flattenSchema();
      for (String fieldName : Arrays.asList(PARENT_FRAME_FIELD_NAME,
                                            CHILD_FRAME_FIELD_NAME,
                                            ROTATION_FIELD_NAME,
                                            ROTATION_X_FIELD_NAME,
                                            ROTATION_Y_FIELD_NAME,
                                            ROTATION_Z_FIELD_NAME,
                                            ROTATION_W_FIELD_NAME,
                                            TRANSLATION_FIELD_NAME,
                                            TRANSLATION_X_FIELD_NAME,
                                            TRANSLATION_Y_FIELD_NAME,
                                            TRANSLATION_Z_FIELD_NAME))
      {
         if (foxgloveFrameTransformSchema.getFields().stream().noneMatch(field -> field.getName().equalsIgnoreCase(fieldName)))
            throw new RuntimeException("Could not find the field " + fieldName + " in the schema for foxglove::FrameTransform");
      }

      TIntObjectHashMap<String> channelIdToTopicMap = new TIntObjectHashMap<>();
      for (MCAP.Record record : mcap.records())
      {
         if (record.op() == MCAP.Opcode.CHANNEL)
         {
            MCAP.Channel channel = (MCAP.Channel) record.body();
            if (channel.schemaId() == foxgloveFrameTransformSchema.getId())
            {
               channelIdToTopicMap.put(channel.id(), channel.topic());
            }
         }
      }
      channelIds.addAll(channelIdToTopicMap.keys());

      Map<String, BasicTransformInfo> allTransforms = new LinkedHashMap<>();

      for (MCAP.Record record : mcap.records())
      {
         if (record.op() == MCAP.Opcode.CHUNK)
         {
            MCAP.Chunk chunk = (MCAP.Chunk) record.body();
            for (MCAP.Record chunkRecord : chunk.records())
            {
               processRecord(chunkRecord, channelIdToTopicMap, allTransforms);
            }
         }
         else
            processRecord(record, channelIdToTopicMap, allTransforms);
      }

      for (BasicTransformInfo transformInfo : allTransforms.values())
      {
         if (!allTransforms.containsKey(transformInfo.parentFrameName()) && !transformInfo.parentFrameName().equals(WORLD_FRAME_NAME))
         {
            unattachedRootNames.add(transformInfo.parentFrameName());
         }
      }

      if (!allTransforms.isEmpty())
      {
         LinkedList<BasicTransformInfo> ordered = sortTransforms(allTransforms);

         while (!ordered.isEmpty())
         {
            BasicTransformInfo basicTransformInfo = ordered.poll();
            YoFoxGloveFrameTransform transform = new YoFoxGloveFrameTransform(basicTransformInfo,
                                                                              rawNameToTransformMap.get(basicTransformInfo.parentFrameName()),
                                                                              inertialFrame,
                                                                              registry);
            yoGraphicGroupDefinition.addChild(YoGraphicDefinitionFactory.newYoGraphicCoordinateSystem3D(transform.rawName,
                                                                                                        transform.poseToRoot,
                                                                                                        0.2,
                                                                                                        ColorDefinitions.SeaGreen()));
            rawNameToTransformMap.put(basicTransformInfo.childFrameName(), transform);
         }
         transformList.addAll(rawNameToTransformMap.values());
         for (YoFoxGloveFrameTransform transform : transformList)
         {
            sanitizedNameToTransformMap.put(transform.sanitizedName, transform);
         }
         yoGraphicGroupDefinition.setVisible(false);
      }
   }

   private void processRecord(MCAP.Record record, TIntObjectHashMap<String> channelIdToTopicMap, Map<String, BasicTransformInfo> allTransforms)
   {
      if (record.op() != MCAP.Opcode.MESSAGE)
         return;

      MCAP.Message message = (MCAP.Message) record.body();
      String topic = channelIdToTopicMap.get(message.channelId());

      if (topic == null)
         return;

      BasicTransformInfo transformInfo = extractFromMessage(foxgloveFrameTransformSchema, topic, message);
      allTransforms.put(transformInfo.childFrameName(), transformInfo);
   }

   private static LinkedList<BasicTransformInfo> sortTransforms(Map<String, BasicTransformInfo> allTransforms)
   {
      LinkedList<BasicTransformInfo> ordered = new LinkedList<>(allTransforms.values());
      ordered.sort((o1, o2) ->
                   {
                      int distanceToRoot1 = 0;
                      int distanceToRoot2 = 0;
                      while (o1 != null)
                      {
                         distanceToRoot1++;
                         o1 = allTransforms.get(o1.parentFrameName());
                      }
                      while (o2 != null)
                      {
                         distanceToRoot1++;
                         o2 = allTransforms.get(o2.parentFrameName());
                      }
                      return Integer.compare(distanceToRoot1, distanceToRoot2);
                   });
      return ordered;
   }

   public void update()
   {
      if (foxgloveFrameTransformSchema == null)
         return;

      for (YoFoxGloveFrameTransform transform : transformList)
      {
         transform.update();
      }
   }

   private final CDRDeserializer cdr = new CDRDeserializer();

   /**
    * Tries to read the given message as a frame transform message.
    *
    * @param message the message to read.
    * @return {@code true} if the message was successfully read, {@code false} otherwise.
    */
   public boolean readMessage(MCAP.Message message)
   {
      if (foxgloveFrameTransformSchema == null)
         return false;

      if (!channelIds.contains(message.channelId()))
         return false;

      cdr.initialize(message.messageBuffer(), message.offsetData(), message.lengthData());

      double rx, ry, rz, rw;
      double tx, ty, tz;
      String parentFrameName;
      String childFrameName;
      try
      {
         List<? extends MCAPSchemaField> fields = foxgloveFrameTransformSchema.getFields();
         rw = 1.0;
         rz = 0.0;
         ry = 0.0;
         rx = 0.0;
         tz = 0.0;
         ty = 0.0;
         tx = 0.0;
         parentFrameName = null;
         childFrameName = null;

         for (int i = 0; i < fields.size(); i++)
         {
            MCAPSchemaField field = fields.get(i);
            if (field.isComplexType())
            {
               if (field.getName().equalsIgnoreCase(ROTATION_FIELD_NAME))
               {
                  MCAPSchemaField xField = fields.get(i + 1);
                  MCAPSchemaField yField = fields.get(i + 2);
                  MCAPSchemaField zField = fields.get(i + 3);
                  MCAPSchemaField wField = fields.get(i + 4);
                  if (!xField.getName().equalsIgnoreCase(ROTATION_X_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + xField.getName());
                  if (!yField.getName().equalsIgnoreCase(ROTATION_Y_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + yField.getName());
                  if (!zField.getName().equalsIgnoreCase(ROTATION_Z_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + zField.getName());
                  if (!wField.getName().equalsIgnoreCase(ROTATION_W_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + wField.getName());
                  rx = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(xField.getType()));
                  ry = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(yField.getType()));
                  rz = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(zField.getType()));
                  rw = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(wField.getType()));
                  i += 4;
               }
               else if (field.getName().equalsIgnoreCase(TRANSLATION_FIELD_NAME))
               {
                  MCAPSchemaField xField = fields.get(i + 1);
                  MCAPSchemaField yField = fields.get(i + 2);
                  MCAPSchemaField zField = fields.get(i + 3);
                  if (!xField.getName().equalsIgnoreCase(TRANSLATION_X_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + xField.getName());
                  if (!yField.getName().equalsIgnoreCase(TRANSLATION_Y_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + yField.getName());
                  if (!zField.getName().equalsIgnoreCase(TRANSLATION_Z_FIELD_NAME))
                     throw new RuntimeException("Unexpected field name: " + zField.getName());
                  tx = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(xField.getType()));
                  ty = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(yField.getType()));
                  tz = cdr.readTypeAsDouble(CDRDeserializer.Type.parseType(zField.getType()));
                  i += 3;
               }
            }
            else if (field.getType().equalsIgnoreCase(FRAME_FIELD_TYPE))
            {
               if (field.getName().equalsIgnoreCase(PARENT_FRAME_FIELD_NAME))
               {
                  parentFrameName = cdr.read_string();
               }
               else if (field.getName().equalsIgnoreCase(CHILD_FRAME_FIELD_NAME))
               {
                  childFrameName = cdr.read_string();
               }
            }
            else
            {
               cdr.skipNext(CDRDeserializer.Type.parseType(field.getType()));
            }
         }
      }
      finally
      {
         cdr.finalize(true);
      }

      YoFoxGloveFrameTransform transform = rawNameToTransformMap.get(childFrameName);
      if (transform != null)
      {
         if (!Objects.equals(parentFrameName, transform.parentFrameName))
            LogTools.error(
                  "Unexpected parent frame name: " + parentFrameName + " for child frame: " + childFrameName + " expected: " + transform.parentFrameName);

         transform.poseToParent.getOrientation().set(rx, ry, rz, rw);
         transform.poseToParent.getPosition().set(tx, ty, tz);
         transform.markPoseToRootAsDirty();
      }
      else
      {
         LogTools.error("Could not find transform for child frame: " + childFrameName);
      }
      return true;
   }

   public YoGraphicDefinition getYoGraphic()
   {
      return yoGraphicGroupDefinition;
   }

   public YoRegistry getRegistry()
   {
      return registry;
   }

   public boolean hasMCAPFrameTransforms()
   {
      return foxgloveFrameTransformSchema != null;
   }

   public MCAP.Schema getMCAPSchema()
   {
      return mcapSchema;
   }

   public MCAPSchema getFrameTransformSchema()
   {
      return foxgloveFrameTransformSchema;
   }

   public YoFoxGloveFrameTransform getTransformFromSanitizedName(String name)
   {
      return sanitizedNameToTransformMap.get(name);
   }

   private static BasicTransformInfo extractFromMessage(MCAPSchema flatSchema, String topic, MCAP.Message message)
   {
      if (!flatSchema.isSchemaFlat())
         throw new IllegalArgumentException("The schema is not flat.");

      CDRDeserializer cdr = new CDRDeserializer();
      cdr.initialize(message.messageBuffer(), message.offsetData(), message.lengthData());

      String parentFrameName = null;
      String childFrameName = null;

      for (MCAPSchemaField field : flatSchema.getFields())
      {
         if (field.isComplexType())
            continue;

         if (field.getType().equalsIgnoreCase(FRAME_FIELD_TYPE))
         {
            if (field.getName().equalsIgnoreCase(PARENT_FRAME_FIELD_NAME))
            {
               parentFrameName = cdr.read_string();
            }
            else if (field.getName().equalsIgnoreCase(CHILD_FRAME_FIELD_NAME))
            {
               childFrameName = cdr.read_string();
            }
         }
         else
         {
            cdr.skipNext(CDRDeserializer.Type.parseType(field.getType()));
         }
      }

      cdr.finalize(true);

      if (parentFrameName == null)
         throw new RuntimeException("Could not find the parent frame name for topic: " + topic);
      return new BasicTransformInfo(topic,
                                    Objects.requireNonNull(parentFrameName, "Parent frame name is null for topic: " + topic + " and child: " + childFrameName),
                                    Objects.requireNonNull(childFrameName, "Child frame name is null for topic: " + topic + " and parent: " + parentFrameName));
   }

   private record BasicTransformInfo(String topic, String parentFrameName, String childFrameName)
   {

   }

   public static class YoFoxGloveFrameTransform
   {
      private final String parentFrameName;
      private final String rawName;
      private final String sanitizedName;
      private YoFoxGloveFrameTransform parent;
      private final List<YoFoxGloveFrameTransform> children;
      private final YoPose3D poseToParent;
      private final YoFramePose3D poseToRoot;

      private boolean isPoseToRootDirty = true;

      private YoFoxGloveFrameTransform(BasicTransformInfo info, YoFoxGloveFrameTransform parent, ReferenceFrame inertialFrame, YoRegistry registry)
      {
         parentFrameName = info.parentFrameName();
         rawName = info.childFrameName();
         sanitizedName = sanitizeName(rawName);
         children = new ArrayList<>();
         String namePrefix = sanitizedName;
         String worldNamePrefix = sanitizeName(namePrefix + "_world");
         poseToParent = new YoPose3D(namePrefix, registry);
         if (parent == null)
         {
            YoPoint3D yoPosition = (YoPoint3D) poseToParent.getPosition();
            YoQuaternion yoOrientation = (YoQuaternion) poseToParent.getOrientation();
            poseToRoot = new YoFramePose3D(new YoFramePoint3D(yoPosition.getYoX(), yoPosition.getYoY(), yoPosition.getYoZ(), inertialFrame),
                                           new YoFrameQuaternion(yoOrientation.getYoQx(),
                                                                 yoOrientation.getYoQy(),
                                                                 yoOrientation.getYoQz(),
                                                                 yoOrientation.getYoQs(),
                                                                 inertialFrame));
         }
         else
         {
            poseToRoot = new YoFramePose3D(worldNamePrefix, inertialFrame, registry);
         }
         setParent(parent);
      }

      private static String sanitizeName(String name)
      {
         name = name.replace('.', '_').replaceAll("_+", "_");
         return name.startsWith("_") ? name.substring(1) : name;
      }

      public void setParent(YoFoxGloveFrameTransform parent)
      {
         if (this.parent != null)
            throw new IllegalStateException("Parent already set.");
         this.parent = parent;
         if (parent != null)
         {
            if (!parent.rawName.equals(parentFrameName))
               throw new IllegalArgumentException("Unexpected parent frame name: " + parent.rawName + " expected: " + parentFrameName);
            parent.addChild(this);
         }
      }

      public void addChild(YoFoxGloveFrameTransform child)
      {
         children.add(child);
      }

      public void markPoseToRootAsDirty()
      {
         isPoseToRootDirty = true;

         for (YoFoxGloveFrameTransform child : children)
         {
            child.markPoseToRootAsDirty();
         }
      }

      public void update()
      {
         if (parent != null && isPoseToRootDirty)
         {
            if (parent.isPoseToRootDirty)
               parent.update();
            poseToRoot.set(parent.poseToRoot);
            poseToRoot.multiply(poseToParent);
         }
         isPoseToRootDirty = false;
      }

      public String getRawName()
      {
         return rawName;
      }

      public String getSanitizedName()
      {
         return sanitizedName;
      }

      public YoFoxGloveFrameTransform getParent()
      {
         return parent;
      }

      public RigidBodyTransformReadOnly getTransformToParent()
      {
         return poseToParent;
      }

      public RigidBodyTransformReadOnly getTransformToRoot()
      {
         return poseToRoot;
      }
   }
}
