package us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.search;

import javafx.scene.control.TreeCell;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class YoRegistryTreeCell extends TreeCell<YoVariableRegistry>
{
   public YoRegistryTreeCell()
   {
      getStyleClass().add("yo-variable-registry-list-cell");
   }

   @Override
   protected void updateItem(YoVariableRegistry registry, boolean empty)
   {
      super.updateItem(registry, empty);

      if (empty)
      {
         setText(null);
      }
      else
      {
         setText(registry.getName());
      }
   }
}
