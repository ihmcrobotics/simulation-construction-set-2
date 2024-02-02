package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPConsoleLogItem;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPLogLevel;
import us.ihmc.scs2.session.mcap.MCAPLogSession;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.simulation.SpyList;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MCAPConsoleLogOutputPaneController
{
   @FXML
   private Pane mainPane;
   @FXML
   private ListView<MCAPConsoleLogItem> consoleOutputListView;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
   }

   public void startSession(MCAPLogSession session)
   {
      // Setup the console output
      consoleOutputListView.setCellFactory(param -> new MCAPConsoleLogItemListCell(session.getMCAPLogFileReader().getCurrentTimestamp()));
      consoleOutputListView.getItems().clear();
      SpyList<MCAPConsoleLogItem> sessionLogItems = session.getMCAPLogFileReader().getConsoleLogManager().getAllConsoleLogItems();
      consoleOutputListView.getItems().setAll(sessionLogItems);
      sessionLogItems.addListener((change) ->
                                  {
                                     if (change.wasAdded())
                                        JavaFXMissingTools.runLater(getClass(), () -> consoleOutputListView.getItems().setAll(sessionLogItems));
                                  });
   }

   public void stopSession()
   {
      consoleOutputListView.getItems().clear();
   }

   private static class MCAPConsoleLogItemListCell extends javafx.scene.control.ListCell<MCAPConsoleLogItem>
   {
      private final Color defaultColor = Color.BLACK;
      private final Map<MCAPLogLevel, Color> logLevelToColorMap = Map.of(MCAPLogLevel.UNKNOWN,
                                                                         defaultColor,
                                                                         MCAPLogLevel.INFO,
                                                                         Color.CORNFLOWERBLUE,
                                                                         MCAPLogLevel.WARNING,
                                                                         Color.ORANGE,
                                                                         MCAPLogLevel.ERROR,
                                                                         Color.RED,
                                                                         MCAPLogLevel.FATAL,
                                                                         Color.DARKRED);

      private final Color futureColor = Color.GRAY.deriveColor(0, 1, 1, 0.5);
      private final Map<MCAPLogLevel, String> logLevelToStringMap = Map.of(MCAPLogLevel.UNKNOWN,
                                                                           "  ???",
                                                                           MCAPLogLevel.INFO,
                                                                           " INFO",
                                                                           MCAPLogLevel.WARNING,
                                                                           " WARN",
                                                                           MCAPLogLevel.ERROR,
                                                                           "ERROR",
                                                                           MCAPLogLevel.FATAL,
                                                                           "FATAL");
      private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS a z");
      private final ZoneId zoneId = ZoneId.systemDefault(); // Need to parameterize this.
      private final YoLong currentTimestamp;
      private YoVariableChangedListener timestampListener;

      public MCAPConsoleLogItemListCell(YoLong currentTimestamp)
      {
         this.currentTimestamp = currentTimestamp;
      }

      @Override
      protected void updateItem(MCAPConsoleLogItem item, boolean empty)
      {
         super.updateItem(item, empty);

         if (empty || item == null)
         {
            setText(null);
            setGraphic(null);
            if (timestampListener != null)
               currentTimestamp.removeListener(timestampListener);
            timestampListener = null;
         }
         else
         {
            setFont(Font.font("Monospaced", 14.0));

            updateTextFill(item);
            timestampListener = v -> updateTextFill(item);
            currentTimestamp.addListener(timestampListener);
            String dateTimeFormatted = dateTimeFormatter.format(item.instant().atZone(zoneId));
            setText("[%s] [%s]\n\t[%s]: %s".formatted(logLevelToStringMap.get(item.logLevel()), dateTimeFormatted, item.processName(), item.message()));
            setGraphic(null);
         }
      }

      private void updateTextFill(MCAPConsoleLogItem item)
      {
         if (item.logTime() > currentTimestamp.getValue())
            setTextFill(futureColor);
         else if (logLevelToColorMap.containsKey(item.logLevel()))
            setTextFill(logLevelToColorMap.get(item.logLevel()));
         else
            setTextFill(logLevelToColorMap.get(MCAPLogLevel.UNKNOWN));
      }
   }
}
