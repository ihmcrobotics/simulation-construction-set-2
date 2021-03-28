package us.ihmc.scs2.sharedMemory.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import us.ihmc.scs2.definition.yoVariable.YoBooleanDefinition;
import us.ihmc.scs2.definition.yoVariable.YoDoubleDefinition;
import us.ihmc.scs2.definition.yoVariable.YoEnumDefinition;
import us.ihmc.scs2.definition.yoVariable.YoIntegerDefinition;
import us.ihmc.scs2.definition.yoVariable.YoLongDefinition;
import us.ihmc.scs2.definition.yoVariable.YoRegistryDefinition;
import us.ihmc.scs2.definition.yoVariable.YoVariableDefinition;
import us.ihmc.scs2.sharedMemory.YoBooleanBuffer;
import us.ihmc.scs2.sharedMemory.YoDoubleBuffer;
import us.ihmc.scs2.sharedMemory.YoEnumBuffer;
import us.ihmc.scs2.sharedMemory.YoIntegerBuffer;
import us.ihmc.scs2.sharedMemory.YoLongBuffer;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.YoVariableBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class SharedMemoryIOTools
{
   private static JAXBContext yoRegistryContext = null;

   public static JAXBContext getYoRegistryContext()
   {
      if (yoRegistryContext == null)
      {
         try
         {
            yoRegistryContext = JAXBContext.newInstance(YoRegistryDefinition.class,
                                                        YoVariableDefinition.class,
                                                        YoBooleanDefinition.class,
                                                        YoDoubleDefinition.class,
                                                        YoIntegerDefinition.class,
                                                        YoLongDefinition.class,
                                                        YoEnumDefinition.class);
         }
         catch (JAXBException e)
         {
            throw new RuntimeException("Problem creating the JAXBContext.", e);
         }
      }

      return yoRegistryContext;
   }

   public static YoRegistryDefinition loadYoRegistryDefinition(InputStream inputStream) throws JAXBException, IOException
   {
      try
      {
         JAXBContext context = getYoRegistryContext();
         Unmarshaller unmarshaller = context.createUnmarshaller();
         return (YoRegistryDefinition) unmarshaller.unmarshal(inputStream);
      }
      finally
      {
         inputStream.close();
      }
   }

   public static void saveYoRegistryDefinition(OutputStream outputStream, YoRegistryDefinition definition) throws JAXBException, IOException
   {
      try
      {
         JAXBContext context = getYoRegistryContext();
         Marshaller marshaller = context.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.marshal(definition, outputStream);
      }
      finally
      {
         outputStream.close();
      }
   }

   public static YoRegistryDefinition toYoRegistryDefinition(YoRegistry yoRegistry)
   {
      return toYoRegistryDefinition(yoRegistry, var -> true, reg -> true);
   }

   public static YoRegistryDefinition toYoRegistryDefinition(YoRegistry yoRegistry, Predicate<YoVariable> variableFilter, Predicate<YoRegistry> registryFilter)
   {
      YoRegistryDefinition definition = new YoRegistryDefinition();
      definition.setName(yoRegistry.getName());
      definition.setYoVariables(yoRegistry.getVariables().stream().filter(variableFilter).map(SharedMemoryIOTools::toYoVariableDefinition)
                                          .collect(Collectors.toList()));
      definition.setYoRegistries(yoRegistry.getChildren().stream().filter(registryFilter).map(SharedMemoryIOTools::toYoRegistryDefinition)
                                           .collect(Collectors.toList()));
      return definition;
   }

   public static YoVariableDefinition toYoVariableDefinition(YoVariable yoVariable)
   {
      YoVariableDefinition definition = null;

      if (yoVariable instanceof YoBoolean)
      {
         definition = new YoBooleanDefinition();
      }
      else if (yoVariable instanceof YoDouble)
      {
         definition = new YoDoubleDefinition();
      }
      else if (yoVariable instanceof YoInteger)
      {
         definition = new YoIntegerDefinition();
      }
      else if (yoVariable instanceof YoLong)
      {
         definition = new YoLongDefinition();
      }
      else if (yoVariable instanceof YoEnum<?>)
      {
         YoEnumDefinition enumDefinition = new YoEnumDefinition();
         enumDefinition.setAllowNullValue(((YoEnum<?>) yoVariable).isNullAllowed());
         enumDefinition.setEnumValuesAsString(((YoEnum<?>) yoVariable).getEnumValuesAsString());
         definition = enumDefinition;
      }
      else
      {
         throw new UnsupportedOperationException("Unsupported yoVariable type: " + yoVariable);
      }

      definition.setName(yoVariable.getName());
      definition.setDescription(yoVariable.getDescription());
      definition.setLowerBound(yoVariable.getLowerBound());
      definition.setUpperBound(yoVariable.getUpperBound());
      return definition;
   }

   public static YoRegistry toYoRegistry(YoRegistryDefinition definition)
   {
      YoRegistry yoRegistry = new YoRegistry(definition.getName());
      if (definition.getYoVariables() != null)
         definition.getYoVariables().forEach(varDefinition -> yoRegistry.addVariable(toYoVariable(varDefinition)));
      if (definition.getYoRegistries() != null)
         definition.getYoRegistries().forEach(regDefinition -> yoRegistry.addChild(toYoRegistry(regDefinition)));
      return yoRegistry;
   }

   public static YoVariable toYoVariable(YoVariableDefinition definition)
   {
      YoVariable yoVariable = null;

      String name = definition.getName();
      String description = definition.getDescription();

      if (definition instanceof YoBooleanDefinition)
      {
         yoVariable = new YoBoolean(name, description, null);
      }
      else if (definition instanceof YoDoubleDefinition)
      {
         yoVariable = new YoDouble(name, description, null);
      }
      else if (definition instanceof YoIntegerDefinition)
      {
         yoVariable = new YoInteger(name, description, null);
      }
      else if (definition instanceof YoLongDefinition)
      {
         yoVariable = new YoLong(name, description, null);
      }
      else if (definition instanceof YoEnumDefinition)
      {
         yoVariable = new YoEnum<>(name,
                                   description,
                                   null,
                                   ((YoEnumDefinition) definition).isAllowNullValue(),
                                   ((YoEnumDefinition) definition).getEnumValuesAsString());
      }
      else
      {
         throw new UnsupportedOperationException("Unsupported definition type: " + definition);
      }

      yoVariable.setVariableBounds(definition.getLowerBound(), definition.getUpperBound());
      return yoVariable;
   }

   public static void exportRegistry(YoRegistry rootRegistry, OutputStream outputStream) throws JAXBException, IOException
   {
      exportRegistry(rootRegistry, outputStream, var -> true, reg -> true);
   }

   public static void exportRegistry(YoRegistry rootRegistry, OutputStream outputStream, Predicate<YoVariable> variableFilter,
                                     Predicate<YoRegistry> registryFilter)
         throws JAXBException, IOException
   {
      saveYoRegistryDefinition(outputStream, toYoRegistryDefinition(rootRegistry, variableFilter, registryFilter));
   }

   public static void exportDataASCII(YoSharedBuffer buffer, OutputStream dataOutputStream)
   {
      exportDataASCII(buffer, dataOutputStream, var -> true, reg -> true);
   }

   public static void exportDataASCII(YoSharedBuffer buffer, OutputStream dataOutputStream, Predicate<YoVariable> variableFilter,
                                      Predicate<YoRegistry> registryFilter)
   {
      YoVariableBufferWriter writer = newASCIIWriter(dataOutputStream);

      for (YoVariableBuffer<?> yoVariableBuffer : buffer.getRegistryBuffer().getYoVariableBuffers())
      {
         if (variableFilter.test(yoVariableBuffer.getYoVariable()))
            exportYoVariableBuffer(yoVariableBuffer, writer);
      }

      writer.closeAndDispose();
   }

   public static YoRegistry importRegistry(InputStream inputStream) throws JAXBException, IOException
   {
      return toYoRegistry(loadYoRegistryDefinition(inputStream));
   }

   public static YoSharedBuffer importDataASCII(InputStream dataInputStream, YoRegistry root) throws IOException
   {
      YoSharedBuffer buffer = new YoSharedBuffer(root, 1);

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

      bufferedReader.lines().forEach(line ->
      {
         int seperatorIndex = line.indexOf(":");
         String variableName = line.substring(0, seperatorIndex).trim();

         String data = line.substring(seperatorIndex, line.length());
         YoVariable yoVariable = root.findVariable(variableName);
         YoVariableBuffer<?> yoVariableBuffer = buffer.getRegistryBuffer().findYoVariableBuffer(yoVariable);

         if (yoVariable instanceof YoBoolean)
         {
            boolean[] bufferData = parseBooleanArray(data);
            growBufferIfNeeded(buffer, bufferData.length);
            System.arraycopy(bufferData, 0, yoVariableBuffer.getBuffer(), 0, bufferData.length);
         }
         else if (yoVariable instanceof YoDouble)
         {
            double[] bufferData = parseDoubleArray(data);
            growBufferIfNeeded(buffer, bufferData.length);
            System.arraycopy(bufferData, 0, yoVariableBuffer.getBuffer(), 0, bufferData.length);
         }
         else if (yoVariable instanceof YoInteger)
         {
            int[] bufferData = parseIntegerArray(data);
            growBufferIfNeeded(buffer, bufferData.length);
            System.arraycopy(bufferData, 0, yoVariableBuffer.getBuffer(), 0, bufferData.length);
         }
         else if (yoVariable instanceof YoLong)
         {
            long[] bufferData = parseLongArray(data);
            growBufferIfNeeded(buffer, bufferData.length);
            System.arraycopy(bufferData, 0, yoVariableBuffer.getBuffer(), 0, bufferData.length);
         }
         else if (yoVariable instanceof YoEnum<?>)
         {
            byte[] bufferData = parseByteArray(data);
            growBufferIfNeeded(buffer, bufferData.length);
            System.arraycopy(bufferData, 0, yoVariableBuffer.getBuffer(), 0, bufferData.length);
         }
         else
         {
            throw new IllegalArgumentException("Unexpected value: " + yoVariable);
         }
      });

      bufferedReader.close();

      return buffer;
   }

   private static void growBufferIfNeeded(YoSharedBuffer buffer, int newSize)
   {
      if (newSize > buffer.getProperties().getSize())
      {
         buffer.resizeBuffer(newSize);
         buffer.setInPoint(0);
         buffer.setOutPoint(newSize - 1);
      }
   }

   private static boolean[] parseBooleanArray(String data)
   {
      data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
      String[] elements = data.split(", ");
      boolean[] parsedArray = new boolean[elements.length];

      for (int i = 0; i < elements.length; i++)
      {
         parsedArray[i] = Boolean.parseBoolean(elements[i]);
      }

      return parsedArray;
   }

   private static double[] parseDoubleArray(String data)
   {
      data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
      String[] elements = data.split(", ");
      double[] parsedArray = new double[elements.length];

      for (int i = 0; i < elements.length; i++)
      {
         parsedArray[i] = Double.parseDouble(elements[i]);
      }

      return parsedArray;
   }

   private static int[] parseIntegerArray(String data)
   {
      data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
      String[] elements = data.split(", ");
      int[] parsedArray = new int[elements.length];

      for (int i = 0; i < elements.length; i++)
      {
         parsedArray[i] = Integer.parseInt(elements[i]);
      }

      return parsedArray;
   }

   private static long[] parseLongArray(String data)
   {
      data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
      String[] elements = data.split(", ");
      long[] parsedArray = new long[elements.length];

      for (int i = 0; i < elements.length; i++)
      {
         parsedArray[i] = Long.parseLong(elements[i]);
      }

      return parsedArray;
   }

   private static byte[] parseByteArray(String data)
   {
      data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
      String[] elements = data.split(", ");
      byte[] parsedArray = new byte[elements.length];

      for (int i = 0; i < elements.length; i++)
      {
         parsedArray[i] = Byte.parseByte(elements[i]);
      }

      return parsedArray;
   }

   private static YoVariableBufferWriter newASCIIWriter(OutputStream outputStream)
   {
      PrintStream printStream = new PrintStream(outputStream);

      return new YoVariableBufferWriter()
      {
         @Override
         public void writeBoolean(String variableName, boolean[] buffer)
         {
            printStream.print(variableName + ": ");
            printStream.println(Arrays.toString(buffer));
         }

         @Override
         public void writeDouble(String variableName, double[] buffer)
         {
            printStream.print(variableName + ": ");
            printStream.println(Arrays.toString(buffer));
         }

         @Override
         public void writeInteger(String variableName, int[] buffer)
         {
            printStream.print(variableName + ": ");
            printStream.println(Arrays.toString(buffer));
         }

         @Override
         public void writeLong(String variableName, long[] buffer)
         {
            printStream.print(variableName + ": ");
            printStream.println(Arrays.toString(buffer));
         }

         @Override
         public void writeEnum(String variableName, String[] enumConstants, byte[] buffer)
         {
            printStream.print(variableName + ": ");
            printStream.println(Arrays.toString(buffer));
         }

         @Override
         public void closeAndDispose()
         {
            printStream.close();
         }
      };
   }

   public static void exportYoVariableBuffer(YoVariableBuffer<?> yoVariableBuffer, YoVariableBufferWriter writer)
   {
      String variableName = yoVariableBuffer.getYoVariable().getFullNameString();
      YoBufferPropertiesReadOnly properties = yoVariableBuffer.getProperties();
      int inPoint = properties.getInPoint();
      int activeBufferLength = properties.getActiveBufferLength();

      if (yoVariableBuffer instanceof YoBooleanBuffer)
      {
         writer.writeBoolean(variableName, (boolean[]) yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample());
      }
      else if (yoVariableBuffer instanceof YoDoubleBuffer)
      {
         writer.writeDouble(variableName, (double[]) yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample());
      }
      else if (yoVariableBuffer instanceof YoIntegerBuffer)
      {
         writer.writeInteger(variableName, (int[]) yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample());
      }
      else if (yoVariableBuffer instanceof YoLongBuffer)
      {
         writer.writeLong(variableName, (long[]) yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample());
      }
      else if (yoVariableBuffer instanceof YoEnumBuffer)
      {
         writer.writeEnum(variableName,
                          ((YoEnum<?>) yoVariableBuffer.getYoVariable()).getEnumValuesAsString(),
                          (byte[]) yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample());
      }
   }

   private static interface YoVariableBufferWriter
   {
      void writeBoolean(String variableName, boolean[] buffer);

      void writeDouble(String variableName, double[] buffer);

      void writeInteger(String variableName, int[] buffer);

      void writeLong(String variableName, long[] buffer);

      void writeEnum(String variableName, String[] enumConstants, byte[] buffer);

      void closeAndDispose();
   }
}
