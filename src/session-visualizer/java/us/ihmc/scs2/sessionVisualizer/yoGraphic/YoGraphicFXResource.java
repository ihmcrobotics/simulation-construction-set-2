package us.ihmc.scs2.sessionVisualizer.yoGraphic;

import java.net.URL;

public class YoGraphicFXResource
{
   private final String resourceName;
   private final URL resourceURL;

   public YoGraphicFXResource(String resourceName)
   {
      this(resourceName, null);
   }

   public YoGraphicFXResource(String resourceName, URL resourceURL)
   {
      this.resourceName = resourceName;
      this.resourceURL = resourceURL;
   }

   public String getResourceName()
   {
      return resourceName;
   }

   public URL getResourceURL()
   {
      return resourceURL;
   }
}
