package us.ihmc.scs2.sessionVisualizer.jfx.tools;

import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TabPaneTools
{
   public static Label editableTabHeader(Tab tab)
   {
      Label headerLabel = new Label();
      TextField headerEditor = new JFXTextField();

      headerLabel.setText(tab.getText());
      tab.setGraphic(headerLabel);
      tab.setText(null);

      headerLabel.setOnMouseClicked(e ->
                                    {
                                       if (e.getClickCount() != 2)// || e.getButton() != MouseButton.PRIMARY)
                                          return;

                                       headerEditor.setText(headerLabel.getText());
                                       headerEditor.setPrefWidth(2.0 * headerLabel.getWidth());
                                       tab.setGraphic(headerEditor);
                                       headerEditor.selectAll();
                                       headerEditor.requestFocus();
                                    });

      headerEditor.setOnAction(e ->
                               {
                                  headerLabel.setText(headerEditor.getText());
                                  tab.setGraphic(headerLabel);
                               });

      headerEditor.focusedProperty().addListener((o, oldValue, newValue) ->
                                                 {
                                                    if (newValue)
                                                       return;

                                                    headerLabel.setText(headerEditor.getText());
                                                    tab.setGraphic(headerLabel);
                                                 });

      return headerLabel;
   }

   public static Function<TabPane, MenuItem> removeMenuItemFactory()
   {
      return tabPane ->
      {
         FontIcon removeIcon = new FontIcon();
         removeIcon.getStyleClass().add("remove-icon-view");
         MenuItem removeMenuItem = new MenuItem("Remove", removeIcon);

         Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
         if (selectedItem == null)
            return null;
         removeMenuItem.setOnAction(e2 -> tabPane.getTabs().remove(selectedItem));

         return removeMenuItem;
      };
   }

   public static Function<TabPane, MenuItem> removeAllMenuItemFactory()
   {
      return removeAllMenuItemFactory(false);
   }

   public static Function<TabPane, MenuItem> removeAllMenuItemFactory(boolean removeOnlyClosableTabs)
   {
      return removeAllMenuItemFactory("Remove all", removeOnlyClosableTabs);
   }

   public static Function<TabPane, MenuItem> removeAllMenuItemFactory(String text, boolean removeOnlyClosableTabs)
   {
      return tabPane ->
      {
         return removeAllMenuItem(text, removeOnlyClosableTabs, tabPane);
      };
   }

   public static MenuItem removeAllMenuItem(String text, boolean removeOnlyClosableTabs, TabPane tabPane)
   {
      FontIcon removeAllIcon = new FontIcon();
      removeAllIcon.getStyleClass().add("remove-icon-view");
      MenuItem removeMenuItem = new MenuItem(text, removeAllIcon);

      removeMenuItem.setOnAction(e2 ->
                                 {
                                    if (tabPane.getTabs().isEmpty())
                                       return;

                                    List<Tab> tabsToclose;
                                    if (removeOnlyClosableTabs)
                                       tabsToclose = tabPane.getTabs().stream().filter(Tab::isClosable).collect(Collectors.toList());
                                    else
                                       tabsToclose = tabPane.getTabs();

                                    if (tabsToclose.isEmpty())
                                       return;

                                    tabPane.getTabs().removeAll(tabsToclose);
                                 });
      return removeMenuItem;
   }

   public static Function<TabPane, MenuItem> addBeforeMenuItemFactory(Supplier<Tab> addAction)
   {
      return addBeforeMenuItemFactory(addAction, "Add before");
   }

   public static Function<TabPane, MenuItem> addBeforeMenuItemFactory(Supplier<Tab> addAction, String title)
   {
      return addBeforeMenuItemFactory(i -> addAction.get(), title);
   }

   public static Function<TabPane, MenuItem> addBeforeMenuItemFactory(IntFunction<Tab> addAction, String title)
   {
      return tabPane ->
      {

         ObservableList<Tab> tabs = tabPane.getTabs();
         int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

         FontIcon addBeforeIcon = new FontIcon();
         addBeforeIcon.getStyleClass().add("add-icon-view");
         MenuItem addBefore = new MenuItem(title, addBeforeIcon);

         addBefore.setOnAction(e2 ->
                               {
                                  Tab newTab = addAction.apply(selectedIndex);
                                  if (newTab != null)
                                  {
                                     tabs.add(selectedIndex, newTab);
                                     tabPane.getSelectionModel().select(selectedIndex);
                                  }
                               });

         return addBefore;
      };
   }

   public static Function<TabPane, MenuItem> addAfterMenuItemFactory(Supplier<Tab> addAction)
   {
      return addAfterMenuItemFactory(addAction, "Add after");
   }

   public static Function<TabPane, MenuItem> addAfterMenuItemFactory(Supplier<Tab> addAction, String title)
   {
      return addAfterMenuItemFactory(i -> addAction.get(), title);
   }

   public static Function<TabPane, MenuItem> addAfterMenuItemFactory(IntFunction<Tab> addAction, String title)
   {
      return tabPane ->
      {
         ObservableList<Tab> tabs = tabPane.getTabs();
         int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

         FontIcon addAfterIcon = new FontIcon();
         addAfterIcon.getStyleClass().add("add-icon-view");
         MenuItem addAfter = new MenuItem(title, addAfterIcon);

         addAfter.setOnAction(e2 ->
                              {
                                 Tab newTab = addAction.apply(selectedIndex + 1);
                                 if (newTab != null)
                                 {
                                    tabs.add(selectedIndex + 1, newTab);
                                    tabPane.getSelectionModel().select(selectedIndex + 1);
                                 }
                              });

         return addAfter;
      };
   }

   public static MenuItem addLastMenuItem(TabPane tabPane, Supplier<Tab> addAction, String title)
   {
      FontIcon addAfterIcon = new FontIcon();
      addAfterIcon.getStyleClass().add("add-icon-view");
      MenuItem addLast = new MenuItem(title, addAfterIcon);

      addLast.setOnAction(e2 ->
                          {
                             Tab newTab = addAction.get();
                             if (newTab != null)
                             {
                                ObservableList<Tab> tabs = tabPane.getTabs();
                                tabs.add(newTab);
                                tabPane.getSelectionModel().selectLast();
                             }
                          });

      return addLast;
   }

   public static MenuItem removeSelectedMenuItem(String title, TabPane tabPane)
   {
      FontIcon removeIcon = new FontIcon();
      removeIcon.getStyleClass().add("remove-icon-view");
      MenuItem removeMenuItem = new MenuItem(title, removeIcon);

      removeMenuItem.setOnAction(e2 ->
                                 {
                                    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
                                    if (selectedIndex < 0)
                                       return;
                                    tabPane.getTabs().remove(selectedIndex);

                                    if (tabPane.getTabs().size() > 0)
                                    {
                                       if (selectedIndex == 0)
                                       {
                                          tabPane.getSelectionModel().select(0);
                                       }
                                       else
                                       {
                                          tabPane.getSelectionModel().select(selectedIndex - 1);
                                       }
                                    }
                                 });

      return removeMenuItem;
   }
}
