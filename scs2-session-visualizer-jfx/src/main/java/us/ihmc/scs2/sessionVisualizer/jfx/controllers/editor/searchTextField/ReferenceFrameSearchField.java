package us.ihmc.scs2.sessionVisualizer.jfx.controllers.editor.searchTextField;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;
import us.ihmc.log.LogTools;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameManager;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.ReferenceFrameWrapper;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ReferenceFrameSearchField extends PropertySearchField<Property<ReferenceFrameWrapper>>
{
   private final ReferenceFrameManager referenceFrameManager;

   public ReferenceFrameSearchField(TextField textField, ReferenceFrameManager referenceFrameManager)
   {
      this(textField, referenceFrameManager, null);
   }

   public ReferenceFrameSearchField(TextField textField, ReferenceFrameManager referenceFrameManager, ImageView validImageView)
   {
      super(textField, validImageView);
      this.referenceFrameManager = referenceFrameManager;
      textField.setText(referenceFrameManager.getWorldFrame().getName());
   }

   @Override
   protected boolean isTextValid(String text)
   {
      if (text == null || text.isEmpty())
         return false;

      ReferenceFrameWrapper referenceFrame = referenceFrameManager.getReferenceFrameFromUniqueName(text);
      if (referenceFrame == null)
         referenceFrame = referenceFrameManager.getReferenceFrameFromFullname(text);
      return referenceFrame != null;
   }

   @Override
   protected String simplifyText(String text)
   {
      if (text == null)
         return referenceFrameManager.getUniqueShortName(referenceFrameManager.getWorldFrame());

      ReferenceFrameWrapper referenceFrame = referenceFrameManager.getReferenceFrameFromFullname(text);
      if (referenceFrame == null)
         return null;

      String uniqueName = referenceFrameManager.getUniqueShortName(referenceFrame);
      if (uniqueName != null && uniqueName.equals(text))
         return null;

      return uniqueName;
   }

   @Override
   protected Callback<ISuggestionRequest, Collection<String>> createSuggestions()
   {
      Collection<String> referenceFrameUniqueNames = referenceFrameManager.getReferenceFrameUniqueShortNames();

      return request ->
      {
         String userText = request.getUserText();
         if (userText.isEmpty())
            return referenceFrameUniqueNames;

         String userTextLowerCase = userText.toLowerCase();
         return referenceFrameUniqueNames.stream().filter(v -> v.toLowerCase().contains(userTextLowerCase)).collect(Collectors.toList());
      };
   }

   @Override
   protected Property<ReferenceFrameWrapper> toSupplier(String text)
   {
      ReferenceFrameWrapper referenceFrame = referenceFrameManager.getReferenceFrameFromUniqueName(text);
      if (referenceFrame == null)
         return null;
      else
         return new SimpleObjectProperty<>(referenceFrame);
   }

   @Override
   protected List<YoComposite> retrieveYoCompositesFromDragboard(Dragboard dragboard)
   {
      LogTools.error("Unsupported operation, implement me!");
      return null;
   }
}
