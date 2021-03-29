package us.ihmc.scs2.sharedMemory.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import us.ihmc.yoVariables.tools.YoSearchTools;
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
      return toYoRegistryDefinition(yoRegistry, null, null);
   }

   public static YoRegistryDefinition toYoRegistryDefinition(YoRegistry yoRegistry, Predicate<YoVariable> variableFilter, Predicate<YoRegistry> registryFilter)
   {
      YoRegistryDefinition definition = new YoRegistryDefinition();
      definition.setName(yoRegistry.getName());

      Stream<YoVariable> variablesStream = yoRegistry.getVariables().stream();
      if (variableFilter != null)
         variablesStream = variablesStream.filter(variableFilter);
      definition.setYoVariables(variablesStream.map(SharedMemoryIOTools::toYoVariableDefinition).collect(Collectors.toList()));

      Stream<YoRegistry> childrenStream = yoRegistry.getChildren().stream();
      if (registryFilter != null)
         childrenStream = childrenStream.filter(registryFilter);
      definition.setYoRegistries(childrenStream.map(SharedMemoryIOTools::toYoRegistryDefinition).collect(Collectors.toList()));

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
      exportRegistry(rootRegistry, outputStream, null, null);
   }

   public static void exportRegistry(YoRegistry rootRegistry, OutputStream outputStream, Predicate<YoVariable> variableFilter,
                                     Predicate<YoRegistry> registryFilter)
         throws JAXBException, IOException
   {
      saveYoRegistryDefinition(outputStream, toYoRegistryDefinition(rootRegistry, variableFilter, registryFilter));
   }

   public static void exportDataASCII(YoSharedBuffer buffer, OutputStream dataOutputStream)
   {
      exportDataASCII(buffer, dataOutputStream, null, null);
   }

   public static void exportDataASCII(YoSharedBuffer buffer, OutputStream outputStream, Predicate<YoVariable> variableFilter,
                                      Predicate<YoRegistry> registryFilter)
   {
      Stream<YoVariableBuffer<?>> yoVariableBufferStream;

      if (registryFilter != null)
      {
         List<YoRegistry> filteredRegistries = YoSearchTools.filterRegistries(registryFilter, buffer.getRootRegistry());
         yoVariableBufferStream = filteredRegistries.stream().flatMap(registry -> registry.getVariables().stream())
                                                    .map(yoVariable -> buffer.getRegistryBuffer().findYoVariableBuffer(yoVariable));
      }
      else
      {
         yoVariableBufferStream = buffer.getRegistryBuffer().getYoVariableBuffers().stream();
      }

      if (variableFilter != null)
      {
         yoVariableBufferStream = yoVariableBufferStream.filter(yoVariableBuffer -> variableFilter.test(yoVariableBuffer.getYoVariable()));
      }

      PrintStream printStream = new PrintStream(outputStream);

      yoVariableBufferStream.forEach(yoVariableBuffer ->
      {
         String variableName = yoVariableBuffer.getYoVariable().getFullNameString();
         YoBufferPropertiesReadOnly properties = yoVariableBuffer.getProperties();
         int inPoint = properties.getInPoint();
         int activeBufferLength = properties.getActiveBufferLength();

         printStream.print(variableName + ": ");
         printStream.println(arrayToString(yoVariableBuffer.copy(inPoint, activeBufferLength, properties).getSample()));
      });

      printStream.close();
   }

   public static void exportDataCSV(YoSharedBuffer buffer, OutputStream dataOutputStream)
   {
      exportDataCSV(buffer, dataOutputStream, null, null);
   }

   public static void exportDataCSV(YoSharedBuffer buffer, OutputStream outputStream, Predicate<YoVariable> variableFilter,
                                    Predicate<YoRegistry> registryFilter)
   {
      Stream<YoVariableBuffer<?>> yoVariableBufferStream;

      if (registryFilter != null)
      {
         List<YoRegistry> filteredRegistries = YoSearchTools.filterRegistries(registryFilter, buffer.getRootRegistry());
         yoVariableBufferStream = filteredRegistries.stream().flatMap(registry -> registry.getVariables().stream())
                                                    .map(yoVariable -> buffer.getRegistryBuffer().findYoVariableBuffer(yoVariable));
      }
      else
      {
         yoVariableBufferStream = buffer.getRegistryBuffer().getYoVariableBuffers().stream();
      }

      if (variableFilter != null)
      {
         yoVariableBufferStream = yoVariableBufferStream.filter(yoVariableBuffer -> variableFilter.test(yoVariableBuffer.getYoVariable()));
      }

      PrintStream printStream = new PrintStream(outputStream);

      YoVariableBuffer<?>[] yoVariableBuffersToExport = yoVariableBufferStream.toArray(YoVariableBuffer[]::new);

      for (int bufferIndex = 0; bufferIndex < yoVariableBuffersToExport.length; bufferIndex++)
      {
         printStream.print(yoVariableBuffersToExport[bufferIndex].getYoVariable().getFullNameString());
         if (bufferIndex < yoVariableBuffersToExport.length - 1)
            printStream.print(", ");
         else
            printStream.println();
      }

      YoBufferPropertiesReadOnly properties = buffer.getProperties();
      int readPosition = properties.getInPoint();

      IntConsumer[] bufferValueWriters = Stream.of(yoVariableBuffersToExport).map(yoVariableBuffer ->
      {
         Object internalBuffer = yoVariableBuffer.getBuffer();

         if (yoVariableBuffer instanceof YoBooleanBuffer)
         {
            boolean[] booleanBuffer = (boolean[]) internalBuffer;
            return (IntConsumer) position -> printStream.print(booleanBuffer[position]);
         }
         if (yoVariableBuffer instanceof YoDoubleBuffer)
         {
            double[] doubleBuffer = (double[]) internalBuffer;
            return (IntConsumer) position -> printStream.print(doubleBuffer[position]);
         }
         if (yoVariableBuffer instanceof YoIntegerBuffer)
         {
            int[] intBuffer = (int[]) internalBuffer;
            return (IntConsumer) position -> printStream.print(intBuffer[position]);
         }
         if (yoVariableBuffer instanceof YoLongBuffer)
         {
            long[] longBuffer = (long[]) internalBuffer;
            return (IntConsumer) position -> printStream.print(longBuffer[position]);
         }
         if (yoVariableBuffer instanceof YoEnumBuffer)
         {
            byte[] byteBuffer = (byte[]) internalBuffer;
            String[] enumConstants = ((YoEnum<?>) yoVariableBuffer.getYoVariable()).getEnumValuesAsString();
            return (IntConsumer) position -> printStream.print(byteBuffer[position] == YoEnum.NULL_VALUE ? YoEnum.NULL_VALUE_STRING
                  : enumConstants[byteBuffer[position]]);
         }

         throw new IllegalArgumentException("Unhandled buffer type: " + yoVariableBuffer);
      }).toArray(IntConsumer[]::new);

      for (int i = 0; i < properties.getActiveBufferLength(); i++)
      {
         for (int bufferIndex = 0; bufferIndex < yoVariableBuffersToExport.length; bufferIndex++)
         {
            bufferValueWriters[bufferIndex].accept(readPosition);
            if (bufferIndex < yoVariableBuffersToExport.length - 1)
               printStream.print(", ");
            else
               printStream.println();
         }

         readPosition = SharedMemoryTools.increment(readPosition, 1, properties.getSize());
      }

      printStream.close();
   }

   private static String arrayToString(Object array)
   {
      if (array instanceof boolean[])
         return Arrays.toString((boolean[]) array);
      if (array instanceof double[])
         return Arrays.toString((double[]) array);
      if (array instanceof int[])
         return Arrays.toString((int[]) array);
      if (array instanceof long[])
         return Arrays.toString((long[]) array);
      if (array instanceof byte[])
         return Arrays.toString((byte[]) array);
      throw new IllegalArgumentException("Unsupported type: " + array);
   }

   public static YoRegistry importRegistry(InputStream inputStream) throws JAXBException, IOException
   {
      return toYoRegistry(loadYoRegistryDefinition(inputStream));
   }

   public static YoSharedBuffer importDataASCII(InputStream inputStream, YoRegistry root) throws IOException
   {
      YoSharedBuffer buffer = new YoSharedBuffer(root, 1);

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

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

   public static YoSharedBuffer importDataCSV(InputStream inputStream, YoRegistry root) throws IOException
   {
      YoSharedBuffer buffer = new YoSharedBuffer(root, 2048);

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

      String currentLine = bufferedReader.readLine();

      int currentPosition = 0;
      String[] variableNames = currentLine.split(", ");

      @SuppressWarnings("unchecked")
      ObjIntConsumer<String>[] bufferValueReaders = Stream.of(variableNames).map(variableName ->
      {
         YoVariable yoVariable = root.findVariable(variableName);
         YoVariableBuffer<?> yoVariableBuffer = buffer.getRegistryBuffer().findYoVariableBuffer(yoVariable);

         Object internalBuffer = yoVariableBuffer.getBuffer();

         if (yoVariableBuffer instanceof YoBooleanBuffer)
         {
            boolean[] booleanBuffer = (boolean[]) internalBuffer;
            return (ObjIntConsumer<String>) (value, position) -> booleanBuffer[position] = Boolean.parseBoolean(value);
         }
         if (yoVariableBuffer instanceof YoDoubleBuffer)
         {
            double[] doubleBuffer = (double[]) internalBuffer;
            return (ObjIntConsumer<String>) (value, position) -> doubleBuffer[position] = Double.parseDouble(value);
         }
         if (yoVariableBuffer instanceof YoIntegerBuffer)
         {
            int[] intBuffer = (int[]) internalBuffer;
            return (ObjIntConsumer<String>) (value, position) -> intBuffer[position] = Integer.parseInt(value);
         }
         if (yoVariableBuffer instanceof YoLongBuffer)
         {
            long[] longBuffer = (long[]) internalBuffer;
            return (ObjIntConsumer<String>) (value, position) -> longBuffer[position] = Long.parseLong(value);
         }
         if (yoVariableBuffer instanceof YoEnumBuffer)
         {
            byte[] byteBuffer = (byte[]) internalBuffer;
            List<String> enumConstants = Arrays.asList(((YoEnum<?>) yoVariable).getEnumValuesAsString());
            return (ObjIntConsumer<String>) (value, position) ->
            {
               if (Objects.equals(value, YoEnum.NULL_VALUE_STRING))
                  byteBuffer[position] = YoEnum.NULL_VALUE;
               else
                  byteBuffer[position] = (byte) enumConstants.indexOf(value);
            };
         }

         throw new IllegalArgumentException("Unhandled buffer type: " + yoVariableBuffer);

      }).toArray(ObjIntConsumer[]::new);

      while ((currentLine = bufferedReader.readLine()) != null)
      {
         if (currentPosition >= buffer.getProperties().getSize())
            buffer.resizeBuffer(2 * buffer.getProperties().getSize());

         String[] currentValues = currentLine.split(", ");
         for (int bufferIndex = 0; bufferIndex < bufferValueReaders.length; bufferIndex++)
         {
            bufferValueReaders[bufferIndex].accept(currentValues[bufferIndex], currentPosition);
         }

         currentPosition++;
      }

      buffer.setInPoint(0);
      buffer.setOutPoint(currentPosition - 1);
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
}
