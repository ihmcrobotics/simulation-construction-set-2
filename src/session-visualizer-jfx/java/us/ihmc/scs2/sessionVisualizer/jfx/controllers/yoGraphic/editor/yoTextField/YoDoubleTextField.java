package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoGraphic.editor.yoTextField;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.util.Callback;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoCompositeSearchManager;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.CompositePropertyTools;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.DragAndDropTools;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoCompositeCollection;
import us.ihmc.yoVariables.variable.YoDouble;

public class YoDoubleTextField extends YoVariableTextField<DoubleProperty>
{
   private final YoCompositeCollection yoVariableCollection;

   public YoDoubleTextField(TextField textField, YoCompositeSearchManager searchManager)
   {
      this(textField, searchManager, null);
   }

   public YoDoubleTextField(TextField textField, YoCompositeSearchManager searchManager, ImageView validImageView)
   {
      super(textField, validImageView);
      yoVariableCollection = searchManager.getYoVariableCollection();
   }

   @Override
   protected boolean isTextValid(String text)
   {
      if (text == null || text.isEmpty())
         return false;

      YoComposite yoComposite = yoVariableCollection.getYoCompositeFromUniqueName(text);
      if (yoComposite == null)
         yoComposite = yoVariableCollection.getYoCompositeFromFullname(text);
      return yoComposite != null || CompositePropertyTools.isParsableAsDouble(text);
   }

   @Override
   protected String simplifyText(String text)
   {
      YoComposite yoComposite = yoVariableCollection.getYoCompositeFromFullname(text);
      return yoComposite == null ? null : yoComposite.getUniqueName();
   }

   @Override
   protected Callback<ISuggestionRequest, Collection<String>> createSuggestions()
   {
      Collection<String> uniqueNameCollection = yoVariableCollection.uniqueNameCollection();

      return request ->
      {
         String userText = request.getUserText();

         if (CompositePropertyTools.isParsableAsDouble(userText))
            return null;

         if (userText.isEmpty())
            return uniqueNameCollection;

         String userTextLowerCase = userText.toLowerCase();
         return uniqueNameCollection.stream().filter(v -> v.toLowerCase().contains(userTextLowerCase)).collect(Collectors.toList());
      };
   }

   @Override
   protected DoubleProperty toSupplier(String text)
   {
      YoComposite yoComposite = yoVariableCollection.getYoCompositeFromUniqueName(text);
      if (yoComposite == null)
         return new SimpleDoubleProperty(Double.parseDouble(text));
      else
         return new YoDoubleProperty((YoDouble) yoComposite.getYoComponents().get(0));
   }

   @Override
   protected List<YoComposite> retrieveYoCompositesFromDragboard(Dragboard dragboard)
   {
      return DragAndDropTools.retrieveYoCompositesFromDragBoard(dragboard, yoVariableCollection);
   }
}
