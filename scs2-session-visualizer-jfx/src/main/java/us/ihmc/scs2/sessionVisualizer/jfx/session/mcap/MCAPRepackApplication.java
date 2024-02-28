package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.jetbrains.annotations.NotNull;
import us.ihmc.commons.thread.ThreadTools;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.session.log.ProgressConsumer;
import us.ihmc.scs2.session.mcap.MCAPLogRepacker;
import us.ihmc.scs2.session.mcap.specs.MCAP;
import us.ihmc.scs2.sessionVisualizer.jfx.SessionVisualizerIOTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXApplicationCreator;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Standalone application to repack an MCAP file.
 * <p>
 * Repacking an MCAP file consists in rebuilding the some of the MCAP records only reusing the minimum data from the original file.
 * This can be useful to recover a MCAP file with corrupted timestamps or index references.
 * </p>
 */
public class MCAPRepackApplication
{

   public static void main(String[] args) throws JSAPException
   {
      String chunkMinDurationMilliseconds = "chunkMinDurationMilliseconds";
      String chunkMaxDurationMilliseconds = "chunkMaxDurationMilliseconds";
      String inputFileName = "inputFileName";
      String outputFileName = "outputFileName";
      String override = "override";

      SimpleJSAP jsap = new SimpleJSAP("MCAP Repack Application",
                                       "This application repacks an MCAP file:"
                                       + " it rebuilds the some of the MCAP records only reusing the minimum data from the original file."
                                       + " This can be useful to recover a MCAP file with corrupted timestamps or index references.",
                                       new Parameter[] {new FlaggedOption(chunkMinDurationMilliseconds,
                                                                          JSAP.INTEGER_PARSER,
                                                                          "50",
                                                                          JSAP.NOT_REQUIRED,
                                                                          'l',
                                                                          "chunkMin",
                                                                          "Minimum duration of a chunk in milliseconds."),
                                                        new FlaggedOption(chunkMaxDurationMilliseconds,
                                                                          JSAP.INTEGER_PARSER,
                                                                          "500",
                                                                          JSAP.NOT_REQUIRED,
                                                                          'h',
                                                                          "chunkMax",
                                                                          "Maximum duration of a chunk in milliseconds."),
                                                        new FlaggedOption(inputFileName,
                                                                          JSAP.STRING_PARSER,
                                                                          null,
                                                                          JSAP.NOT_REQUIRED,
                                                                          'i',
                                                                          "input",
                                                                          "Input file to repack. If not provided a file chooser will be opened."),
                                                        new FlaggedOption(outputFileName,
                                                                          JSAP.STRING_PARSER,
                                                                          null,
                                                                          JSAP.NOT_REQUIRED,
                                                                          'o',
                                                                          "output",
                                                                          "Output file repacked. If not provided a file chooser will be opened."),
                                                        new FlaggedOption(override,
                                                                          JSAP.BOOLEAN_PARSER,
                                                                          "false",
                                                                          JSAP.NOT_REQUIRED,
                                                                          'f',
                                                                          "force",
                                                                          "If true, the output file will be overwritten if it already exists.")});
      JSAPResult config = jsap.parse(args);

      if (jsap.messagePrinted())
      {
         System.out.println(jsap.getUsage());
         System.out.println(jsap.getHelp());
         return;
      }

      int minDuration = config.getInt(chunkMinDurationMilliseconds);
      int maxDuration = config.getInt(chunkMaxDurationMilliseconds);
      String input = config.getString(inputFileName);
      String output = config.getString(outputFileName);
      boolean force = config.getBoolean(override);

      File inputFile = null;

      if (input != null)
      {
         inputFile = new File(input);
         if (!inputFile.exists())
         {
            System.err.println("Cannot find input file: " + inputFile.getAbsolutePath());
            System.exit(0);
            return;
         }
      }

      boolean javaFXStarted = false;

      if (inputFile == null)
      {
         JavaFXApplicationCreator.spawnJavaFXMainApplication();
         Platform.setImplicitExit(false);
         javaFXStarted = true;

         LogTools.info("No input file provided, opening file chooser.");
         inputFile = JavaFXMissingTools.runAndWait(MCAPRepackApplication.class,
                                                   () -> SessionVisualizerIOTools.showOpenDialog(null,
                                                                                                 "Select input file",
                                                                                                 new FileChooser.ExtensionFilter("MCAP files", "*.mcap"),
                                                                                                 MCAPLogSessionManagerController.LOG_FILE_KEY));
      }

      if (inputFile == null)
      {
         System.err.println("No input file selected.");
         System.exit(0);
         return;
      }

      File outputFile = null;

      if (output != null)
      {
         outputFile = new File(output);
         if (outputFile.exists())
         {
            if (force)
            {
               outputFile.delete();
            }
            else
            {
               LogTools.error("Output file already exists: " + outputFile.getAbsolutePath() + ". Use the -f option to override it.");
               System.exit(0);
               return;
            }
         }
      }
      else
      {
         if (!javaFXStarted)
         {
            JavaFXApplicationCreator.spawnJavaFXMainApplication();
            Platform.setImplicitExit(false);
            javaFXStarted = true;
         }

         LogTools.info("No output file provided, opening file chooser.");
         outputFile = JavaFXMissingTools.runAndWait(MCAPRepackApplication.class,
                                                    () -> SessionVisualizerIOTools.showSaveDialog(null,
                                                                                                  "Select output file",
                                                                                                  new FileChooser.ExtensionFilter("MCAP files", "*.mcap"),
                                                                                                  MCAPLogSessionManagerController.LOG_FILE_KEY));
      }

      if (outputFile == null)
      {
         System.err.println("No output file selected.");
         System.exit(0);
         return;
      }

      MCAPLogRepacker repacker = new MCAPLogRepacker();
      LogTools.info("Repacking MCAP file: " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());

      try (FileInputStream fileInputStream = new FileInputStream(inputFile); FileOutputStream outputStream = new FileOutputStream(outputFile))
      {
         ProgressConsumer progressConsumer = newSimpleProgressConsumer();
         repacker.repack(new MCAP(fileInputStream.getChannel()), minDuration, maxDuration, outputStream, progressConsumer);
      }
      catch (Exception e)
      {
         LogTools.error("Failed to repack MCAP file: " + e.getMessage());
         e.printStackTrace();
      }
      finally
      {
         System.exit(0);
      }
   }

   @NotNull
   private static ProgressConsumer newSimpleProgressConsumer()
   {
      ProgressBar progressBar = new ProgressBar("Repacking",
                                                100,
                                                10,
                                                false,
                                                true,
                                                System.out,
                                                ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
                                                "",
                                                1L,
                                                false,
                                                null,
                                                ChronoUnit.SECONDS,
                                                0L,
                                                Duration.ZERO);

      return new ProgressConsumer()
      {
         @Override
         public void started(String task)
         {
         }

         @Override
         public void info(String info)
         {
         }

         @Override
         public void error(String error)
         {
         }

         @Override
         public void progress(double progressPercentage)
         {
            progressBar.stepTo(Math.round(progressPercentage * 100.0));
         }

         @Override
         public void done()
         {
            progressBar.stepTo(100);
            ThreadTools.sleep(200);

            LogTools.info("Repacking done.");
         }
      };
   }
}
