package us.ihmc.scs2.sessionVisualizer.jfx.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import us.ihmc.log.LogTools;
import us.ihmc.scs2.definition.configuration.SCSGuiConfigurationDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositeDefinition;
import us.ihmc.scs2.definition.yoComposite.YoCompositePatternListDefinition;
import us.ihmc.scs2.definition.yoEntry.YoEntryConfigurationDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicDefinition;
import us.ihmc.scs2.definition.yoGraphic.YoGraphicListDefinition;
import us.ihmc.scs2.definition.yoSlider.YoSliderboardListDefinition;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositePattern;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeTools;

public class XMLTools
{
   private static boolean loadingYoGraphicContext = false;
   private static JAXBContext yoGraphicContext;

   static
   {
      Thread loader = new Thread(XMLTools::loadResourcesNow, "JAXBContext Loader");
      loader.setPriority(Thread.MIN_PRIORITY);
      loader.setDaemon(true);
      loader.start();
   }

   public static void loadResources()
   {
      // Only need to load this class to get the resources loaded.
   }

   public static void loadResourcesNow()
   {
      if (loadingYoGraphicContext || isYoGraphicContextReady())
         return;

      loadingYoGraphicContext = true;

      try
      {
         Predicate<? super Class<?>> classFilter = type -> !Modifier.isAbstract(type.getModifiers()) && !type.isInterface();

         Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(YoGraphicDefinition.class.getPackageName()))
                                                                             .setScanners(new SubTypesScanner()));

         Set<Class<? extends YoGraphicDefinition>> graphicDefinitions = reflections.getSubTypesOf(YoGraphicDefinition.class).stream().filter(classFilter)
                                                                                   .collect(Collectors.toSet());

         reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(YoCompositeDefinition.class.getPackageName()))
                                                                 .setScanners(new SubTypesScanner()));
         Set<Class<? extends YoCompositeDefinition>> compositeDefinitions = reflections.getSubTypesOf(YoCompositeDefinition.class).stream().filter(classFilter)
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

   public static YoSliderboardListDefinition loadYoSliderboardListDefinition(InputStream inputStream) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoSliderboardListDefinition.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return (YoSliderboardListDefinition) unmarshaller.unmarshal(inputStream);
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

   public static void saveYoSliderboardListDefinition(OutputStream outputStream, YoSliderboardListDefinition definition) throws JAXBException
   {
      JAXBContext context = JAXBContext.newInstance(YoSliderboardListDefinition.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(definition, outputStream);
   }
}
