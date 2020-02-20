package us.ihmc.scs2.sharedMemory;

import java.util.Objects;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.sharedMemory.tools.BufferTools;
import us.ihmc.yoVariables.variable.*;

public abstract class LinkedYoVariable<T extends YoVariable<T>> extends LinkedBuffer
{
   protected final T linkedYoVariable;
   protected final YoVariableBuffer<T> buffer;

   protected PushRequest<T> pushRequestToProcess;
   protected PullRequest<T> pullRequest;

   protected BufferSampleRequest bufferSampleRequest;
   @SuppressWarnings("rawtypes")
   protected BufferSample bufferSample;

   @SuppressWarnings({"rawtypes", "unchecked"})
   static LinkedYoVariable<?> newLinkedYoVariable(YoVariable<?> yoVariableToLink, YoVariableBuffer<?> buffer)
   {
      if (yoVariableToLink instanceof YoBoolean)
         return new LinkedYoBoolean((YoBoolean) yoVariableToLink, (YoBooleanBuffer) buffer);
      if (yoVariableToLink instanceof YoDouble)
         return new LinkedYoDouble((YoDouble) yoVariableToLink, (YoDoubleBuffer) buffer);
      if (yoVariableToLink instanceof YoInteger)
         return new LinkedYoInteger((YoInteger) yoVariableToLink, (YoIntegerBuffer) buffer);
      if (yoVariableToLink instanceof YoLong)
         return new LinkedYoLong((YoLong) yoVariableToLink, (YoLongBuffer) buffer);
      if (yoVariableToLink instanceof YoEnum)
         return new LinkedYoEnum<>((YoEnum) yoVariableToLink, (YoEnumBuffer) buffer);

      throw new UnsupportedOperationException("Unsupported YoVariable type: " + yoVariableToLink.getClass().getSimpleName());
   }

   LinkedYoVariable(T yoVariable, YoVariableBuffer<T> buffer)
   {
      Objects.requireNonNull(buffer, "Cannot create a linked YoVariable without a buffer.");

      this.linkedYoVariable = yoVariable;
      this.buffer = buffer;
   }

   public void requestEntireBuffer()
   {
      requestBufferWindow(-1, -1);
   }

   public void requestActiveBufferOnly()
   {
      requestBufferWindow(-1, -2);
   }

   public void requestBufferStartingFrom(int from)
   {
      requestBufferWindow(from, -1);
   }

   public void requestBufferWindow(int from, int length)
   {
      bufferSampleRequest = new BufferSampleRequest(from, length);
   }

   @Override
   public void push()
   {
      pushRequestToProcess = toPushRequest();
   }

   @Override
   boolean processPush()
   {
      if (pushRequestToProcess == null)
         return false;

      PushRequest<T> push = pushRequestToProcess;
      pushRequestToProcess = null;

      return push.push();
   }

   @Override
   void flushPush()
   {
      pushRequestToProcess = null;
   }

   @Override
   void prepareForPull()
   {
      pullRequest = toPullRequest();
      consumeBufferSampleRequest();
   }

   private void consumeBufferSampleRequest()
   {
      if (bufferSampleRequest == null)
         return;

      BufferSampleRequest localRequest = bufferSampleRequest;
      bufferSampleRequest = null;

      int from = localRequest.getFrom();
      int length = localRequest.getLength();

      YoBufferPropertiesReadOnly properties = buffer.getProperties();

      if (length == -1)
      {
         if (from == -1)
         {
            from = 0;
            length = properties.getSize();
         }
         else if (from >= 0)
         {
            length = BufferTools.computeSubLength(from, properties.getOutPoint(), properties.getSize());
         }
      }
      else if (length == -2 && from == -1)
      {
         from = properties.getInPoint();
         length = properties.getActiveBufferLength();
      }

      if (length == 0)
         return;

      if (from < 0 || from >= properties.getSize() || length < 0 || length > properties.getSize())
         throw new IllegalArgumentException("Invalid request: from = " + from + ", length = " + length);

      bufferSample = buffer.copy(from, length);
   }

   @Override
   public boolean pull()
   {
      PullRequest<T> pull = pullRequest;
      pullRequest = null;

      if (pull != null)
      {
         pull.pull();
      }

      return pull != null;
   }

   @Override
   public boolean hasRequestPending()
   {
      return bufferSampleRequest != null;
   }

   public boolean isRequestedBufferSampleAvailable()
   {
      return bufferSample != null;
   }

   @SuppressWarnings("rawtypes")
   public BufferSample pollRequestedBufferSample()
   {
      BufferSample localSample = bufferSample;
      bufferSample = null;
      return localSample;
   }

   abstract PullRequest<T> toPullRequest();

   abstract PushRequest<T> toPushRequest();

   public T getLinkedYoVariable()
   {
      return linkedYoVariable;
   }

   YoVariableBuffer<T> getBuffer()
   {
      return buffer;
   }
}