package us.ihmc.scs2;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import us.ihmc.messager.MessagerAPIFactory.MessagerAPI;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.messager.SharedMemoryMessager;

public class BufferedMessager extends SharedMemoryMessager
{
   private final HashMap<Topic<?>, AtomicReference<?>> internalBuffer = new HashMap<>();

   public BufferedMessager(MessagerAPI messagerAPI)
   {
      super(messagerAPI);
      for (Topic<?> topic : messagerAPI.getAllTopics())
         internalBuffer.put(topic, super.createInput(topic, null));
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> AtomicReference<T> createInput(Topic<T> topic, T defaultValue)
   {
      if (defaultValue == null)
         return super.createInput(topic, (T) internalBuffer.get(topic).get());
      else
         return super.createInput(topic, defaultValue);
   }
}
