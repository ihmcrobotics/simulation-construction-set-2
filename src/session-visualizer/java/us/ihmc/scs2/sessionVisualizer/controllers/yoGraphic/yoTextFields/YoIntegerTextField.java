package us.ihmc.scs2.sessionVisualizer.controllers.yoGraphic.yoTextFields;

import java.util.Collection;
import java.util.List;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.util.Callback;
import us.ihmc.scs2.sessionVisualizer.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoCompositeCollection;
import us.ihmc.yoVariables.variable.YoInteger;

public class YoIntegerTextField extends YoVariableTextField<IntegerProperty>
{
   private final YoCompositeCollection yoIntegerCollection;
   private final boolean isInputOptional;

   public YoIntegerTextField(TextField textField, YoCompositeSearchManager searchManager)
   {
      this(textField, searchManager, null);
   }

   public YoIntegerTextField(TextField textField, YoCompositeSearchManager searchManager, ImageView validImageView)
   {
      this(textField, searchManager, false, validImageView);
   }

   public YoIntegerTextField(TextField textField, YoCompositeSearchManager searchManager, boolean isInputOptional, ImageView validImageView)
   {
      super(textField, validImageView);
      this.isInputOptional = isInputOptional;

      yoIntegerCollection = searchManager.getYoIntegerCollection();
   }

   @Override
   protected boolean isTextValid(String text)
   {
      if (text == null || text.isEmpty())
         return isInputOptional;

      YoComposite yoComposite = yoIntegerCollection.getYoCompositeFromUniqueName(text);
      if (yoComposite == null)
         yoComposite = yoIntegerCollection.getYoCompositeFromFullname(text); // TODO Happens when loading file, needs to update TextField to use unique name.
      return yoComposite != null || CompositePropertyTools.isParsableAsInteger(text);
   }

   @Override
   protected String simplifyText(String text)
   {
      YoComposite yoComposite = yoIntegerCollection.getYoCompositeFromFullname(text);
      return yoComposite == null ? null : yoComposite.getUniqueName();
   }

   @Override
   protected Callback<ISuggestionRequest, Collection<String>> createSuggestions()
   {
      return request ->
      {
         if (CompositePropertyTools.isParsableAsInteger(request.getUserText()))
            return null;
         else
            return yoIntegerCollection.uniqueNameCollection();
      };
   }

   @Override
   protected IntegerProperty toSupplier(String text)
   {
      if (isInputOptional && text == null)
         return null;

      YoComposite yoComposite = yoIntegerCollection.getYoCompositeFromUniqueName(text);
      if (yoComposite == null)
         return new SimpleIntegerProperty(Integer.parseInt(text));
      else
         return new YoIntegerProperty((YoInteger) yoComposite.getYoComponents().get(0));
   }

   @Override
   protected List<YoComposite> retrieveYoCompositesFromDragboard(Dragboard dragboard)
   {
      return DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoIntegerCollection);
   }
}
