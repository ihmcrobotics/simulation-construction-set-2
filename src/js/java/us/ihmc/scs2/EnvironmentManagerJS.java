package us.ihmc.scs2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.scene.SubScene;
import us.ihmc.scs2.definition.terrain.TerrainObjectDefinition;
import us.ihmc.scs2.protobuf.ThreeProto;
import us.ihmc.scs2.session.Session;
import us.ihmc.scs2.sessionVisualizer.managers.BackgroundExecutorManager;
import us.ihmc.scs2.websocket.JavalinManager;
import us.ihmc.scs2.websocket.JavalinWebsocketHandler;

public class EnvironmentManagerJS implements Manager
{
   private final List<ThreeProto.MeshGroup> environmentMeshGroups = new ArrayList<>();
   private final ConcurrentLinkedQueue<org.eclipse.jetty.websocket.api.Session> activeWebSocketSessions = new ConcurrentLinkedQueue<>();

   public EnvironmentManagerJS(JavalinManager javalinManager, BackgroundExecutorManager backgroundExecutorManager)
   {
      JavalinWebsocketHandler handler = javalinManager.webSocket("/viepwort");
      handler.addOnConnect(ctx -> onSessionOpen(ctx.session));
      handler.addOnClose(ctx -> onSessionClose(ctx.session));
   }

   private void onSessionOpen(org.eclipse.jetty.websocket.api.Session session)
   {
      for (ThreeProto.MeshGroup mesh : environmentMeshGroups)
      {
//         session.getRemote().sendBytesByFuture(mesh.toByteString().asReadOnlyByteBuffer());
      }
      activeWebSocketSessions.add(session);
   }

   private void onSessionClose(org.eclipse.jetty.websocket.api.Session session)
   {
      activeWebSocketSessions.remove(session);
   }

   public void addWorldCoordinateSystem(double size)
   {
      // TODO Implement me
   }

   public void addSkybox(SubScene subScene)
   {
      // TODO Implement me
   }

   @Override
   public void startSession(Session session)
   {
      List<TerrainObjectDefinition> terrainObjectDefinitions = session.getTerrainObjectDefinitions();
      for (TerrainObjectDefinition definition : terrainObjectDefinitions)
         environmentMeshGroups.add(ThreeProtoTools.toProtoMeshGroup(definition.getVisualDefinitions()));
   }

   @Override
   public void stopSession()
   {
      // TODO Implement me
   }

   @Override
   public boolean isSessionLoaded()
   {
      return true;
   }
}
