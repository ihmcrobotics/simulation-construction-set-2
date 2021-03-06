package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.search;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.DoubleStringConverter;
import us.ihmc.javaFXExtensions.control.LongSpinnerValueFactory;
import us.ihmc.javaFXExtensions.control.UnboundedDoubleSpinnerValueFactory;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoBooleanProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoEnumAsStringProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoLongProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ScientificDoubleStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

// FIXME Need to manually do some cleanup when the cell is being updated.
public class YoCompositeListCell extends ListCell<YoComposite>
{
   private static final double DOUBLE_SPINNER_STEP_SIZE = 0.1;

   private static final double GRAPHIC_PREF_WIDTH = 100.0;

   private final YoManager yoManager;
   private ListView<YoComposite> owner;

   private final ReadOnlyBooleanProperty showUniqueName;

   private YoComposite yoComposite;
   private Labeled yoCompositeNameDisplay = this;

   public YoCompositeListCell(YoManager yoManager, ReadOnlyBooleanProperty showUniqueName, ListView<YoComposite> owner)
   {
      this.yoManager = yoManager;
      this.showUniqueName = showUniqueName;
      this.owner = owner;
      getStyleClass().add("yo-variable-list-cell");
   }

   @Override
   protected void updateItem(YoComposite yoComposite, boolean empty)
   {
      this.yoComposite = yoComposite;
      super.updateItem(yoComposite, empty);

      prefWidthProperty().bind(owner.widthProperty().subtract(15.0));
      setMinWidth(100.0);

      if (empty || yoManager.getLinkedRootRegistry() == null)
      {
         setGraphic(null);
         setText(null);
         setTooltip(null);
         return;
      }

      if (yoComposite.getPattern().getComponentIdentifiers() == null)
      {
         YoVariable yoVariable = YoVariable.class.cast(yoComposite.getYoComponents().get(0));

         Region yoVariableControl = createYoVariableControl(yoVariable, yoManager.getLinkedRootRegistry());
         setGraphic(yoVariableControl);
         setContentDisplay(ContentDisplay.LEFT);
         setAlignment(Pos.CENTER_LEFT);
         setGraphicTextGap(5);
         yoCompositeNameDisplay = this;
      }
      else
      {
         List<Region> yoVariableControls = createYoVariableControls(yoComposite.getYoComponents(), yoManager.getLinkedRootRegistry());

         GridPane cellGraphic = new GridPane();
         cellGraphic.setHgap(5.0);
         cellGraphic.setVgap(2.0);

         for (int i = 0; i < yoVariableControls.size(); i++)
         {
            YoVariable component = yoComposite.getYoComponents().get(i);
            String componentIdentifier = yoComposite.getPattern().getComponentIdentifiers()[i];
            Label idLabel = new Label(componentIdentifier);
            idLabel.setTooltip(new Tooltip(component.getName() + "\n" + component.getNamespace()));
            Region componentControl = yoVariableControls.get(i);
            cellGraphic.getChildren().addAll(idLabel, componentControl);
            GridPane.setConstraints(idLabel, 0, i);
            GridPane.setConstraints(componentControl, 1, i);
         }
         Label label = new Label();
         yoCompositeNameDisplay = label;
         label.setFont(Font.font("System", FontWeight.BOLD, 12.0));
         setGraphic(new VBox(3, label, cellGraphic));
         setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
         setAlignment(Pos.TOP_LEFT);
         setGraphicTextGap(0);
         setText(null);
         setTooltip(null);
      }

      updateYoCompositeName(showUniqueName.get());
      showUniqueName.addListener((o, oldValue, newValue) -> updateYoCompositeName(newValue));
      yoCompositeNameDisplay.setTooltip(new Tooltip(yoComposite.getName() + "\n" + yoComposite.getNamespace()));
   }

   private void updateYoCompositeName(boolean showUniqueName)
   {
      if (yoCompositeNameDisplay != null && yoComposite != null)
         yoCompositeNameDisplay.setText(showUniqueName ? yoComposite.getUniqueName() : yoComposite.getName());
   }

   public static List<Region> createYoVariableControls(Collection<YoVariable> yoVariables, LinkedYoRegistry linkedRegistry)
   {
      return yoVariables.stream().map(v -> createYoVariableControl(v, linkedRegistry)).collect(Collectors.toList());
   }

   public static Region createYoVariableControl(YoVariable yoVariable, LinkedYoRegistry linkedRegistry)
   {
      if (yoVariable instanceof YoDouble)
         return createYoDoubleControl(new YoDoubleProperty((YoDouble) yoVariable), linkedRegistry);
      if (yoVariable instanceof YoBoolean)
         return createYoBooleanControl(new YoBooleanProperty((YoBoolean) yoVariable), linkedRegistry);
      if (yoVariable instanceof YoLong)
         return createYoLongControl(new YoLongProperty((YoLong) yoVariable), linkedRegistry);
      if (yoVariable instanceof YoInteger)
         return createYoIntegerControl(new YoIntegerProperty((YoInteger) yoVariable), linkedRegistry);
      if (yoVariable instanceof YoEnum)
         return createYoEnumControl(new YoEnumAsStringProperty<>((YoEnum<?>) yoVariable), linkedRegistry);
      throw new UnsupportedOperationException("Unhandled YoVariable type: " + yoVariable.getClass().getSimpleName());
   }

   public static Control createYoDoubleControl(YoDoubleProperty yoDoubleProperty, LinkedYoRegistry linkedRegistry)
   {
      UnboundedDoubleSpinnerValueFactory valueFactory = new UnboundedDoubleSpinnerValueFactory(Double.NEGATIVE_INFINITY,
                                                                                               Double.POSITIVE_INFINITY,
                                                                                               yoDoubleProperty.getValue(),
                                                                                               DOUBLE_SPINNER_STEP_SIZE);
      DoubleStringConverter rawDoubleStringConverter = new DoubleStringConverter();
      ScientificDoubleStringConverter scientificDoubleStringConverter = new ScientificDoubleStringConverter(3);
      valueFactory.setConverter(scientificDoubleStringConverter);
      Spinner<Double> spinner = new Spinner<>(valueFactory);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      spinner.focusedProperty().addListener((o, oldValue, newValue) ->
      {
         valueFactory.setConverter(newValue ? rawDoubleStringConverter : scientificDoubleStringConverter);
      });
      yoDoubleProperty.bindDoubleProperty(spinner.getValueFactory().valueProperty(), () -> linkedRegistry.push(yoDoubleProperty.getYoVariable()));

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public static Region createYoBooleanControl(YoBooleanProperty yoBooleanProperty, LinkedYoRegistry linkedRegistry)
   {
      CheckBox checkBox = new CheckBox();
      HBox root = new HBox(checkBox);
      root.setPrefWidth(GRAPHIC_PREF_WIDTH);
      root.alignmentProperty().set(Pos.CENTER_LEFT);
      checkBox.setSelected(yoBooleanProperty.getValue());
      yoBooleanProperty.bindBooleanProperty(checkBox.selectedProperty(), () -> linkedRegistry.push(yoBooleanProperty.getYoVariable()));

      return root;
   }

   public static Control createYoLongControl(YoLongProperty yoLongProperty, LinkedYoRegistry linkedRegistry)
   {
      Spinner<Long> spinner = new Spinner<>(new LongSpinnerValueFactory(Long.MIN_VALUE, Long.MAX_VALUE, yoLongProperty.getValue(), 1L));
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      yoLongProperty.bindLongProperty(spinner.getValueFactory().valueProperty(), () -> linkedRegistry.push(yoLongProperty.getYoVariable()));

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public static Control createYoIntegerControl(YoIntegerProperty yoIntegerProperty, LinkedYoRegistry linkedRegistry)
   {
      Spinner<Integer> spinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, yoIntegerProperty.getValue(), 1);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      yoIntegerProperty.bindIntegerProperty(spinner.getValueFactory().valueProperty(), () -> linkedRegistry.push(yoIntegerProperty.getYoVariable()));

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public static <E extends Enum<E>> Control createYoEnumControl(YoEnumAsStringProperty<E> yoEnumProperty, LinkedYoRegistry linkedRegistry)
   {
      ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(yoEnumProperty.getYoVariable().getEnumValuesAsString()));
      comboBox.setValue(yoEnumProperty.getValue());
      comboBox.setPrefWidth(GRAPHIC_PREF_WIDTH);
      comboBox.setEditable(false);
      yoEnumProperty.bindStringProperty(comboBox.valueProperty(), () -> linkedRegistry.push(yoEnumProperty.getYoVariable()));

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(comboBox.valueProperty());
      comboBox.setTooltip(tooltip);

      return comboBox;
   }
}