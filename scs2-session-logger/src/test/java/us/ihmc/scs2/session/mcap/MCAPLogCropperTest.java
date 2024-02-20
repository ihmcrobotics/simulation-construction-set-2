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
import java.nio.file.StandardCopyOption;

public class MCAPLogCropperTest
{
   @Test
   public void testNoCropMCAPDemoFile() throws IOException
   {
      URL demoMCAPURL = new URL("https://github.com/foxglove/mcap/raw/main/testdata/mcap/demo.mcap");
      File demoMCAPFile = downloadFile(demoMCAPURL);

      MCAP mcap = new MCAP(new FileInputStream(demoMCAPFile).getChannel());
      MCAPLogCropper mcapLogCropper = new MCAPLogCropper(mcap);
      mcapLogCropper.setStartTimestamp(0);
      mcapLogCropper.setEndTimestamp(Long.MAX_VALUE);
      mcapLogCropper.setOutputFormat(OutputFormat.MCAP);
      File croppedDemoMCAPFile = createTempMCAPFile("croppedDemo");
      mcapLogCropper.crop(new FileOutputStream(croppedDemoMCAPFile));

      // Now let's compare the original and the cropped files
      FileInputStream originalFileInputStream = new FileInputStream(demoMCAPFile);
      FileInputStream croppedFileInputStream = new FileInputStream(croppedDemoMCAPFile);

      try
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
      finally
      {
         originalFileInputStream.close();
         croppedFileInputStream.close();
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
