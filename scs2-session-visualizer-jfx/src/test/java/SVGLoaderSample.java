
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.javainthebox.caraibe.svg.SVGContent;
import net.javainthebox.caraibe.svg.SVGLoader;

public class SVGLoaderSample extends Application
{

   @Override
   public void start(Stage stage)
   {
      SVGContent content = SVGLoader.load(getClass().getResource("duke.svg").toString());

      Scene scene = new Scene(content, 1024, 768);

      stage.setScene(scene);
      stage.setTitle("SVGLoader Sample");
      stage.show();
   }

   public static void main(String[] args)
   {
      Application.launch(args);
   }
}
