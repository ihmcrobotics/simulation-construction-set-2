package us.ihmc.scs2.sessionVisualizer.tools;

import java.util.Objects;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ApplicationRunner extends Application
{
   private static Application applicationToRun;

   public static void startApplication(Application application)
   {
      applicationToRun = application;
      Application.launch();
   }

   @Override
   public void init() throws Exception
   {
      Objects.requireNonNull(applicationToRun);
      applicationToRun.init();
   }

   @Override
   public void start(Stage primaryStage) throws Exception
   {
      applicationToRun.start(primaryStage);
   }

   @Override
   public void stop() throws Exception
   {
      applicationToRun.stop();
      applicationToRun = null;
   }

   public static Runnable runApplication(Application launcher)
   {
      Runnable runnable = new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               launcher.start(new Stage());
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      };

      PlatformImpl.startup(() ->
      {
         Platform.runLater(runnable);
      });
      PlatformImpl.setImplicitExit(false);

      return runnable;
   }
}
