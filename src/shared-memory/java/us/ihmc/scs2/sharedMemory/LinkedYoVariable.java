package us.ihmc.scs2.sharedMemory;

import java.util.Objects;

import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.yoVariables.variable.*;

public abstract class LinkedYoVariable<T extends YoVariable<T>> extends LinkedBuffer
{
   protected final T linkedYoVariable;
   protected final YoVariableBuffer<T> buffer;

   protected YoBufferPropertiesReadOnly currentBufferProperties;

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

   @Override
   public void push()
   {
      pushRequestToProcess = toPushRequest();
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
   boolean processPush()
   {
      if (pushRequestToProcess == null)
         return false;

      PushRequest<T> push = pushRequestToProcess;
      pushRequestToProcess = null;

      return push.push();
   }

   @Override
   void prepareForPull(YoBufferPropertiesReadOnly newProperties)
   {
      currentBufferProperties = newProperties;
      pullRequest = toPullRequest();

      BufferSampleRequest localRequest = bufferSampleRequest;
      bufferSampleRequest = null;
      if (localRequest != null)
      {
         int from = localRequest.getFrom();
         int length = localRequest.getLength();
         if (length == -1)
         {
            if (from == -1)
            {
               from = 0;
               length = newProperties.getSize();
            }
            else
            {
               if (currentBufferProperties.getOutPoint() < from)
               {
                  length = currentBufferProperties.getOutPoint() - from + currentBufferProperties.getSize();
               }
               else
               {
                  length = currentBufferProperties.getOutPoint() - from + 1;
               }
            }
         }
         else if (from == -1)
         {
            from = 0;

            if (length == -2)
            {
               from = currentBufferProperties.getInPoint();
               length = currentBufferProperties.getActiveBufferLength();

               if (length <= 0)
                  return;
            }
         }

         bufferSample = buffer.copy(from, length);
      }
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

   public YoBufferPropertiesReadOnly peekCurrentBufferProperties()
   {
      return currentBufferProperties;
   }

   public YoBufferPropertiesReadOnly pollCurrentBufferProperties()
   {
      YoBufferPropertiesReadOnly properties = currentBufferProperties;
      currentBufferProperties = null;
      return properties;
   }

   @Override
   public boolean hasBufferSampleRequestPending()
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