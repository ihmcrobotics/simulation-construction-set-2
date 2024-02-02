package us.ihmc.scs2.sessionVisualizer.jfx.session.mcap;

import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.commons.lang3.mutable.MutableLong;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPConsoleLogItem;
import us.ihmc.scs2.session.mcap.MCAPConsoleLogManager.MCAPLogLevel;
import us.ihmc.scs2.session.mcap.MCAPLogFileReader;
import us.ihmc.scs2.session.mcap.MCAPLogSession;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.SessionVisualizerToolkit;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.JavaFXMissingTools;
import us.ihmc.scs2.sharedMemory.interfaces.YoBufferPropertiesReadOnly;
import us.ihmc.scs2.simulation.SpyList;
import us.ihmc.yoVariables.listener.YoVariableChangedListener;
import us.ihmc.yoVariables.variable.YoLong;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

public class MCAPConsoleLogOutputPaneController
{
   @FXML
   private Pane mainPane;
   @FXML
   private ListView<MCAPConsoleLogItem> consoleOutputListView;

   private EventHandler<MouseEvent> goToLogEventMouseEventHandler;
   private Consumer<YoBufferPropertiesReadOnly> scrollLastLogItemListener;
   private MCAPLogSession session;

   public void initialize(SessionVisualizerToolkit toolkit)
   {
   }

   public void startSession(MCAPLogSession session)
   {
      this.session = session;
      // Setup the console output
      MCAPLogFileReader mcapLogFileReader = session.getMCAPLogFileReader();
      consoleOutputListView.setCellFactory(param -> new MCAPConsoleLogItemListCell(param, session, mcapLogFileReader.getCurrentTimestamp()));
      consoleOutputListView.getItems().clear();
      SpyList<MCAPConsoleLogItem> sessionLogItems = mcapLogFileReader.getConsoleLogManager().getAllConsoleLogItems();
      consoleOutputListView.getItems().setAll(sessionLogItems);
      sessionLogItems.addListener((change) ->
                                  {
                                     if (change.wasAdded())
                                        JavaFXMissingTools.runLater(getClass(), () -> consoleOutputListView.getItems().setAll(sessionLogItems));
                                  });

      consoleOutputListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

      MutableLong requestingLogTimestamp = new MutableLong(Long.MIN_VALUE);

      goToLogEventMouseEventHandler = event ->
      {
         if (event.getButton() != MouseButton.PRIMARY)
            return;
         if (event.getClickCount() != 2)
            return;
         if (!event.isStillSincePress())
            return;

         int selectedIndex = consoleOutputListView.getSelectionModel().getSelectedIndex();
         if (selectedIndex < 0)
            return;
         MCAPConsoleLogItem selectedLogItem = consoleOutputListView.getItems().get(selectedIndex);
         requestingLogTimestamp.setValue(selectedLogItem.logTime());
         int logIndex = mcapLogFileReader.getIndexFromTimestamp(selectedLogItem.logTime());
         session.submitLogPositionRequest(logIndex);
      };
      scrollLastLogItemListener = new Consumer<>()
      {
         private long lastTimestamp = Long.MIN_VALUE;
         private int lastSearchResult = Integer.MIN_VALUE;

         @Override
         public void accept(YoBufferPropertiesReadOnly bufferProperties)
         {
            long currentTimestamp = session.getMCAPLogFileReader().getCurrentTimestamp().getValue();

            if (requestingLogTimestamp.getValue() != Long.MIN_VALUE && requestingLogTimestamp.getValue() == currentTimestamp)
            {
               requestingLogTimestamp.setValue(Long.MIN_VALUE);
               lastTimestamp = currentTimestamp;
               return;
            }

            if (currentTimestamp == lastTimestamp)
               return;
            lastTimestamp = currentTimestamp;

            int searchResult = Collections.binarySearch(consoleOutputListView.getItems(),
                                                        new MCAPConsoleLogItem(currentTimestamp, null, null, null, null, null, 0),
                                                        Comparator.comparingLong(MCAPConsoleLogItem::logTime));
            if (searchResult < 0)
               searchResult = -searchResult - 1;
            if (searchResult == lastSearchResult)
               return;
            lastSearchResult = searchResult;
            int finalSearchResult = Math.max(0, searchResult - 2);
            JavaFXMissingTools.runLater(MCAPConsoleLogOutputPaneController.this.getClass(), () -> consoleOutputListView.scrollTo(finalSearchResult));
         }
      };

      consoleOutputListView.addEventFilter(MouseEvent.MOUSE_CLICKED, goToLogEventMouseEventHandler);
      session.addCurrentBufferPropertiesListener(scrollLastLogItemListener);
   }

   public void stopSession()
   {
      consoleOutputListView.getItems().clear();
      if (goToLogEventMouseEventHandler != null)
         consoleOutputListView.removeEventFilter(MouseEvent.MOUSE_CLICKED, goToLogEventMouseEventHandler);
      goToLogEventMouseEventHandler = null;
      if (scrollLastLogItemListener != null)
         session.removeCurrentBufferPropertiesListener(scrollLastLogItemListener);
      scrollLastLogItemListener = null;
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
      private final ListView<MCAPConsoleLogItem> owner;
      private final MCAPLogSession session;
      private final YoLong currentTimestamp;
      private YoVariableChangedListener timestampListener;

      public MCAPConsoleLogItemListCell(ListView<MCAPConsoleLogItem> owner, MCAPLogSession session, YoLong currentTimestamp)
      {
         this.owner = owner;
         this.session = session;
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

            DoubleBinding cellWidthProperty = owner.widthProperty().subtract(15);
            minWidthProperty().bind(cellWidthProperty);
            prefWidthProperty().bind(cellWidthProperty);
            maxWidthProperty().bind(cellWidthProperty);

            setWrapText(true);
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
