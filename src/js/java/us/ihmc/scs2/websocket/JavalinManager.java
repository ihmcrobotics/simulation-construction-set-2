package us.ihmc.scs2.websocket;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import io.javalin.Javalin;

public class JavalinManager
{
   private final Javalin app;
   private int port;
   private final Map<String, JavalinWebsocketHandler> handlers = new HashMap<>();

   public JavalinManager()
   {
      app = Javalin.create(config -> config.addStaticFiles("/public"));
   }

   public void start(int port)
   {
      this.port = port;
      app.start(port);
      app.get("index", handler -> handler.res.sendRedirect("index.html"));
   }

   public void startBrowser()
   {
      String s = String.format("http://localhost:%d/index", port);
      Desktop desktop = Desktop.getDesktop();

      try
      {
         desktop.browse(URI.create(s));
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public JavalinWebsocketHandler webSocket(String path)
   {
      JavalinWebsocketHandler handler = handlers.get(path);
      if (handler == null)
      {
         handler = new JavalinWebsocketHandler();
//         handlers.put(path, handler);
//         app.ws(path, handler.setupAsWsHandlerConsumer());
      }
      return handler;
   }
}
