package us.ihmc.scs2.symbolic.parser;

import us.ihmc.scs2.definition.yoVariable.YoEquationDefinition.EquationInputDefinition;
import us.ihmc.scs2.definition.yoVariable.YoVariableDefinition;
import us.ihmc.scs2.sharedMemory.YoDoubleBuffer;
import us.ihmc.scs2.sharedMemory.YoIntegerBuffer;
import us.ihmc.scs2.sharedMemory.YoSharedBuffer;
import us.ihmc.scs2.sharedMemory.YoVariableBuffer;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.SharedMemoryIOTools;
import us.ihmc.scs2.symbolic.EquationInput;
import us.ihmc.scs2.symbolic.EquationInput.DoubleVariable;
import us.ihmc.scs2.symbolic.EquationInput.IntegerVariable;
import us.ihmc.scs2.symbolic.EquationInput.ScalarVariable;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.tools.YoTools;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YoEquationInputHandler
{
   private final List<YoRegistry> yoRegistries = new ArrayList<>();
   private YoSharedBuffer yoSharedBuffer;

   private final List<YoScalarVariableBufferHolder<?>> allBufferHolders = new ArrayList<>();

   public YoEquationInputHandler()
   {
   }

   public void addRegistry(YoRegistry yoRegistry)
   {
      yoRegistries.add(yoRegistry);
   }

   public void setYoSharedBuffer(YoSharedBuffer yoSharedBuffer)
   {
      this.yoSharedBuffer = yoSharedBuffer;
      if (!yoRegistries.contains(yoSharedBuffer.getRootRegistry()))
         yoRegistries.add(yoSharedBuffer.getRootRegistry());
   }

   public boolean hasBuffer()
   {
      return yoSharedBuffer != null;
   }

   public YoBufferPropertiesReadOnly getBufferProperties()
   {
      return yoSharedBuffer.getProperties();
   }

   public void setHistoryUpdate(boolean enable)
   {
      for (YoScalarVariableBufferHolder<?> bufferHolder : allBufferHolders)
         bufferHolder.setHistoryUpdate(enable);
   }

   public void setHistoryIndex(int index)
   {
      for (YoScalarVariableBufferHolder<?> bufferHolder : allBufferHolders)
         bufferHolder.setHistoryIndex(index);
   }

   public YoEquationInputHandler duplicate()
   {
      YoEquationInputHandler duplicate = new YoEquationInputHandler();
      duplicate.yoRegistries.addAll(yoRegistries);
      duplicate.yoSharedBuffer = yoSharedBuffer;
      return duplicate;
   }

   private YoVariable searchYoVariable(String name)
   {
      int separatorIndex = name.lastIndexOf(YoTools.NAMESPACE_SEPERATOR_STRING);

      String namespaceEnding = separatorIndex == -1 ? null : name.substring(0, separatorIndex);
      String variableName = separatorIndex == -1 ? name : name.substring(separatorIndex + 1);

      for (YoRegistry yoRegistry : yoRegistries)
      {
         YoVariable yoVariable = yoRegistry.findVariable(namespaceEnding, variableName);

         if (yoVariable != null)
            return yoVariable;
      }
      return null;
   }

   public EquationInput searchYoEquationInput(YoVariableDefinition definition)
   {
      if (definition == null)
         return null;
      // TODO Should check the type
      return searchYoEquationInput("%s%s%s".formatted(definition.getNamespace(), YoTools.NAMESPACE_SEPERATOR_STRING, definition.getName()));
   }

   public EquationInput searchYoEquationInput(String name)
   {
      YoVariable yoVariable = searchYoVariable(name);

      if (yoVariable == null)
         return null;

      if (yoSharedBuffer != null)
      {
         YoVariableBuffer<?> yoVariableBuffer = yoSharedBuffer.getRegistryBuffer().findYoVariableBuffer(yoVariable);
         if (yoVariableBuffer != null)
         {
            YoScalarVariableBufferHolder<?> yoScalarVariableBufferHolder = newYoVariableBufferHolder(yoVariableBuffer);
            allBufferHolders.add(yoScalarVariableBufferHolder);
            return yoScalarVariableBufferHolder;
         }
      }

      return newYoVariableHolder(yoVariable);
   }

   private static YoScalarVariableHolder<?> newYoVariableHolder(YoVariable yoVariable)
   {
      Objects.requireNonNull(yoVariable, "YoVariable cannot be null.");
      if (yoVariable instanceof YoDouble)
         return new YoDoubleVariable((YoDouble) yoVariable);
      else if (yoVariable instanceof YoInteger)
         return new YoIntegerVariable((YoInteger) yoVariable);
      else
         throw new IllegalArgumentException("Unsupported YoVariable type: " + yoVariable.getClass().getSimpleName());
   }

   private static YoScalarVariableBufferHolder<?> newYoVariableBufferHolder(YoVariableBuffer<?> yoVariableBuffer)
   {
      Objects.requireNonNull(yoVariableBuffer, "YoVariableBuffer cannot be null.");
      if (yoVariableBuffer instanceof YoDoubleBuffer)
         return new YoDoubleVariableBufferHolder((YoDoubleBuffer) yoVariableBuffer);
      else if (yoVariableBuffer instanceof YoIntegerBuffer)
         return new YoIntegerVariableBufferHolder((YoIntegerBuffer) yoVariableBuffer);
      else
         throw new IllegalArgumentException("Unsupported YoVariableBuffer type: " + yoVariableBuffer.getClass().getSimpleName());
   }

   private abstract static class YoScalarVariableHolder<V extends YoVariable> implements ScalarVariable
   {
      protected final V yoVariable;

      public YoScalarVariableHolder(V yoVariable)
      {
         this.yoVariable = Objects.requireNonNull(yoVariable, "YoVariable cannot be null.");
      }

      @Override
      public String valueAsString()
      {
         return yoVariable.getFullNameString();
      }

      @Override
      public EquationInputDefinition toInputDefinition()
      {
         return new EquationInputDefinition(SharedMemoryIOTools.toYoVariableDefinition(yoVariable));
      }
   }

   private static class YoDoubleVariable extends YoScalarVariableHolder<YoDouble> implements DoubleVariable
   {
      public YoDoubleVariable(YoDouble yoDouble)
      {
         super(yoDouble);
      }

      @Override
      public void setValue(double value)
      {
         yoVariable.set(value);
      }

      @Override
      public double getAsDouble()
      {
         return yoVariable.getValue();
      }
   }

   private static class YoIntegerVariable extends YoScalarVariableHolder<YoInteger> implements IntegerVariable
   {
      public YoIntegerVariable(YoInteger yoInteger)
      {
         super(yoInteger);
      }

      @Override
      public void setValue(int value)
      {
         yoVariable.set(value);
      }

      @Override
      public int getAsInt()
      {
         return yoVariable.getValue();
      }
   }

   private abstract static class YoScalarVariableBufferHolder<V extends YoVariable> implements ScalarVariable
   {
      protected final YoVariableBuffer<V> yoBuffer;
      /**
       * When enabled, this variable will read directly from the buffer at the desired index instead of the current value of the YoVariable.
       */
      protected boolean historyUpdateEnabled = false;
      /**
       * When {@link #historyUpdateEnabled} is enabled, this variable will read from the buffer at this index.
       */
      protected int historyIndex = 0;

      public YoScalarVariableBufferHolder(YoVariableBuffer<V> yoBuffer)
      {
         this.yoBuffer = Objects.requireNonNull(yoBuffer, "YoBuffer cannot be null.");
      }

      @Override
      public String valueAsString()
      {
         return yoBuffer.getYoVariable().getFullNameString();
      }

      @Override
      public EquationInputDefinition toInputDefinition()
      {
         return new EquationInputDefinition(SharedMemoryIOTools.toYoVariableDefinition(yoBuffer.getYoVariable()));
      }

      void setHistoryUpdate(boolean enable)
      {
         historyUpdateEnabled = enable;
      }

      void setHistoryIndex(int index)
      {
         historyIndex = index;
      }
   }

   private static class YoDoubleVariableBufferHolder extends YoScalarVariableBufferHolder<YoDouble> implements DoubleVariable
   {
      public YoDoubleVariableBufferHolder(YoDoubleBuffer yoBuffer)
      {
         super(yoBuffer);
      }

      @Override
      public void setValue(double value)
      {
         if (historyUpdateEnabled)
            ((double[]) yoBuffer.getBuffer())[historyIndex] = value;
         else
            yoBuffer.getYoVariable().set(value);
      }

      @Override
      public double getAsDouble()
      {
         if (historyUpdateEnabled)
            return ((double[]) yoBuffer.getBuffer())[historyIndex];
         else
            return yoBuffer.getYoVariable().getValue();
      }
   }

   private static class YoIntegerVariableBufferHolder extends YoScalarVariableBufferHolder<YoInteger> implements IntegerVariable
   {
      public YoIntegerVariableBufferHolder(YoIntegerBuffer yoBuffer)
      {
         super(yoBuffer);
      }

      @Override
      public void setValue(int value)
      {
         if (historyUpdateEnabled)
            ((int[]) yoBuffer.getBuffer())[historyIndex] = value;
         else
            yoBuffer.getYoVariable().set(value);
      }

      @Override
      public int getAsInt()
      {
         if (historyUpdateEnabled)
            return ((int[]) yoBuffer.getBuffer())[historyIndex];
         else
            return yoBuffer.getYoVariable().getValue();
      }
   }
}
