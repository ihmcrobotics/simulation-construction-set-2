package us.ihmc.scs2.session.mcap;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.mcap.MCAPLogCropper.OutputFormat;
import us.ihmc.scs2.session.mcap.output.MCAPDataOutput;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.session.mcap.specs.records.Magic;

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
   public void testSimpleCloningMCAP() throws IOException
   {
      File demoMCAPFile = getDemoMCAPFile();
      MCAP originalMCAP = new MCAP(new FileInputStream(demoMCAPFile).getChannel());
      File clonedDemoMCAPFile = createTempMCAPFile("clonedDemo");
      MCAPDataOutput dataOutput = MCAPDataOutput.wrap(new FileOutputStream(clonedDemoMCAPFile).getChannel());
      dataOutput.putBytes(Magic.MAGIC_BYTES); // header magic
      originalMCAP.records().forEach(record -> record.write(dataOutput));
      dataOutput.putBytes(Magic.MAGIC_BYTES); // footer magic
      dataOutput.close();

      // Let's compare the original and the cloned files by loading them into memory and comparing their content
      MCAP clonedMCAP = new MCAP(new FileInputStream(clonedDemoMCAPFile).getChannel());

      if (originalMCAP.records().size() != clonedMCAP.records().size())
      {
         fail("Original and cloned MCAPs have different number of records");
      }

      for (int i = 0; i < originalMCAP.records().size(); i++)
      {
         assertEquals(originalMCAP.records().get(i), clonedMCAP.records().get(i), "Record " + i + " is different");
      }

      assertFileEquals(demoMCAPFile, clonedDemoMCAPFile);
   }

   @Test
   public void testNotActuallyCroppingMCAPDemoFile() throws IOException
   {
      File demoMCAPFile = getDemoMCAPFile();

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
      assertFileEquals(demoMCAPFile, croppedDemoMCAPFile);
   }

   /**
    * Compares the content of two files. This is a simple byte-to-byte comparison.
    *
    * @param expected The expected file.
    * @param actual   The actual file.
    */
   private static void assertFileEquals(File expected, File actual) throws IOException
   {
      try (FileInputStream expectedFileInputStream = new FileInputStream(expected); FileInputStream actualFileInputStream = new FileInputStream(actual))
      {
         byte[] expectedBuffer = new byte[1024];
         byte[] actualBuffer = new byte[1024];

         int expectedRead = 0;
         int actualRead = 0;

         while ((expectedRead = expectedFileInputStream.read(expectedBuffer)) != -1)
         {
            actualRead = actualFileInputStream.read(actualBuffer);
            if (actualRead == -1)
            {
               fail("Actual file is shorter than the expected file");
            }

            if (expectedRead != actualRead)
            {
               fail("Files have different lengths");
            }

            for (int i = 0; i < expectedRead; i++)
            {
               if (expectedBuffer[i] != actualBuffer[i])
               {
                  fail("Files are different");
               }
            }
         }
      }
   }

   private static File getDemoMCAPFile() throws IOException
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
      return demoMCAPFile;
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
