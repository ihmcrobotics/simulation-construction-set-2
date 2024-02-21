package us.ihmc.scs2.session.mcap;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAPLogCropper.OutputFormat;
import us.ihmc.scs2.session.mcap.specs.MCAP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

public class MCAPLogCropperTest
{
   @Test
   public void testNoCropMCAPDemoFile() throws IOException
   {
      File demoMCAPFile;
      // Check if the demo file is already downloaded, allowing for faster local testing
      Path localFileVersion = Paths.get(System.getProperty("user.home"), "Downloads", "demo.mcap");
      if (Files.exists(localFileVersion))
      {
         demoMCAPFile = localFileVersion.toFile();
      }
      else
      {
         URL demoMCAPURL = new URL("https://github.com/foxglove/mcap/raw/main/testdata/mcap/demo.mcap");
         demoMCAPFile = downloadFile(demoMCAPURL);
      }

      MCAP originalMCAP = new MCAP(new FileInputStream(demoMCAPFile).getChannel());
      MCAPLogCropper mcapLogCropper = new MCAPLogCropper(originalMCAP);
      mcapLogCropper.setStartTimestamp(0);
      mcapLogCropper.setEndTimestamp(Long.MAX_VALUE);
      mcapLogCropper.setOutputFormat(OutputFormat.MCAP);
      File croppedDemoMCAPFile = createTempMCAPFile("croppedDemo");
      mcapLogCropper.crop(new FileOutputStream(croppedDemoMCAPFile));

      // Let's compare the original and the cropped files by loading them into memory and comparing their content
      MCAP croppedMCAP = new MCAP(new FileInputStream(croppedDemoMCAPFile).getChannel());

      if (originalMCAP.records().size() != croppedMCAP.records().size())
      {
         fail("Original and cropped MCAPs have different number of records");
      }

      for (int i = 0; i < originalMCAP.records().size(); i++)
      {
         assertEquals(originalMCAP.records().get(i), croppedMCAP.records().get(i));
      }

      // Now let's compare the original and the cropped files
      try (FileInputStream originalFileInputStream = new FileInputStream(demoMCAPFile);
           FileInputStream croppedFileInputStream = new FileInputStream(croppedDemoMCAPFile))
      {
         byte[] originalBuffer = new byte[1024];
         byte[] croppedBuffer = new byte[1024];

         int originalRead = 0;
         int croppedRead = 0;

         while ((originalRead = originalFileInputStream.read(originalBuffer)) != -1)
         {
            croppedRead = croppedFileInputStream.read(croppedBuffer);
            if (croppedRead == -1)
            {
               throw new IOException("Cropped file is shorter than the original file");
            }

            if (originalRead != croppedRead)
            {
               throw new IOException("Original and cropped files have different lengths");
            }

            for (int i = 0; i < originalRead; i++)
            {
               if (originalBuffer[i] != croppedBuffer[i])
               {
                  throw new IOException("Original and cropped files are different");
               }
            }
         }
      }
   }

   private static File downloadFile(URL url) throws IOException
   {
      File file = createTempMCAPFile(FilenameUtils.getBaseName(url.getFile()));
      LogTools.info("Downloading file from " + url);
      try (InputStream in = url.openStream())
      {
         Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      LogTools.info("Downloaded file to " + file.getAbsolutePath());
      return file;
   }

   private static File createTempMCAPFile(String name) throws IOException
   {
      File file = File.createTempFile(name, ".mcap");
      LogTools.info("Created temporary file: " + file.getAbsolutePath());
      //      file.deleteOnExit();
      return file;
   }
}
