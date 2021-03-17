package us.ihmc.scs2.sessionVisualizer.jfx.xml;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.Test;

public class XMLToolsTest
{

   @Test
   public void test() throws JAXBException
   {
      XMLTools.loadResourcesNow();
      XMLTools.loadYoGraphicListDefinition(getClass().getClassLoader().getResourceAsStream("yoGraphic/AtlasDefaultWalking.scs2.yoGraphic"));
   }
}
