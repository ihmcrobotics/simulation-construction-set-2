package us.ihmc.scs2.sessionVisualizer.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.reflections.Reflections;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.configuration.SCSGuiConfigurationDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositeDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositePatternListDefinition;
import us.ihmc.scs2.definition.yoEntry.YoEntryConfigurationDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicListDefinition;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoCompositePattern;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoCompositeTools;

public class XMLTools
{
   private static JAXBContext yoGraphicContext;

   static
   {
      Thread loader = new Thread(() ->
      {
         try
         {
            Reflections reflections = new Reflections();
            Set<Class<? extends YoGraphicDefinition>> graphicDefinitions = reflections.getSubTypesOf(YoGraphicDefinition.class).stream()
                                                                                      .filter(type -> !Modifier.isAbstract(type.getModifiers())
                                                                                            && !type.isInterface())
                                                                                      .collect(Collectors.toSet());
            Set<Class<? extends YoCompositeDefinition>> compositeDefinitions = reflections.getSubTypesOf(YoCompositeDefinition.class).stream()
                                                                                          .filter(type -> !Modifier.isAbstract(type.getModifiers())
                                                                                                && !type.isInterface())
                                                                                          .collect(Collectors.toSet());
            List<Class<?>> classesToBeBound = new ArrayList<>();
            classesToBeBound.add(YoGraphicListDefinition.class);
            classesToBeBound.addAll(graphicDefinitions);
            classesToBeBound.addAll(compositeDefinitions);
            yoGraphicContext = JAXBContext.newInstance(classesToBeBound.toArray(new Class[classesToBeBound.size()]));
         }
         catch (JAXBException e)
         {
            throw new RuntimeException("Failed at creating JAXBContext, won't be able to load YoGraphics. Cause: ", e);
         }
      }, "JAXBContext Loader");
      loader.setPriority(Thread.MIN_PRIORITY);
      loader.setDaemon(true);
      loader.start();
   }

   public static void loadResources()
   {
      // Only need to load this class to get the resources loaded.
   }

   public static boolean isYoGraphicContextReady()
   {
      return yoGraphicContext != null;
   }

   public static SCSGuiConfigurationDefinition loadSCSGuiConfigurationDefinition(InputStream inputStream) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(SCSGuiConfigurationDefinition.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return (SCSGuiConfigurationDefinition) unmarshaller.unmarshal(inputStream);
   }

   public static List<YoCompositePattern> loadYoCompositePatterns(InputStream inputStream) throws JAXBException
   {
      return YoCompositeTools.toYoCompositePatterns(loadYoCompositePatternListDefinition(inputStream));
   }

   public static YoGraphicListDefinition loadYoGraphicListDefinition(InputStream inputStream) throws JAXBException
   {
      if (yoGraphicContext == null)
      {
         LogTools.error("Context not loaded, unable to load YoGraphics.");
         return null;
      }
      Unmarshaller unmarshaller = yoGraphicContext.createUnmarshaller();
      return (YoGraphicListDefinition) unmarshaller.unmarshal(inputStream);
   }

   public static YoCompositePatternListDefinition loadYoCompositePatternListDefinition(InputStream inputStream) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoCompositePatternListDefinition.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return (YoCompositePatternListDefinition) unmarshaller.unmarshal(inputStream);
   }

   public static YoEntryConfigurationDefinition loadYoEntryConfigurationDefinition(InputStream inputStream) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoEntryConfigurationDefinition.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return (YoEntryConfigurationDefinition) unmarshaller.unmarshal(inputStream);
   }

   public static void saveSCSGuiConfigurationDefinition(OutputStream outputStream, SCSGuiConfigurationDefinition definition) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(SCSGuiConfigurationDefinition.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(definition, outputStream);
   }

   public static void saveYoGraphicListDefinition(OutputStream outputStream, YoGraphicListDefinition definition) throws JAXBException
   {
      if (yoGraphicContext == null)
      {
         LogTools.error("Context not loaded, unable to save YoGraphics.");
         return;
      }
      Marshaller marshaller = yoGraphicContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(definition, outputStream);
   }

   public static void saveYoCompositePatterns(OutputStream outputStream, List<YoCompositePattern> yoCompositePatterns) throws JAXBException
   {
      saveYoCompositePatternListDefinition(outputStream, YoCompositeTools.toYoCompositePatternListDefinition(yoCompositePatterns));
   }

   public static void saveYoCompositePatternListDefinition(OutputStream outputStream, YoCompositePatternListDefinition definition) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoCompositePatternListDefinition.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(definition, outputStream);
   }

   public static void saveYoEntryConfigurationDefinition(OutputStream outputStream, YoEntryConfigurationDefinition definition) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoEntryConfigurationDefinition.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(definition, outputStream);
   }
}
