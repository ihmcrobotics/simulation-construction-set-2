package us.ihmc.scs2.sessionVisualizer.jfx.controllers.yoComposite.search;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.DoubleStringConverter;
import us.ihmc.javaFXExtensions.control.LongSpinnerValueFactory;
import us.ihmc.scs2.sessionVisualizer.jfx.YoNameDisplay;
import us.ihmc.scs2.sessionVisualizer.jfx.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoBooleanProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoEnumAsStringProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoLongProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.properties.YoVariableProperty;
import us.ihmc.scs2.sessionVisualizer.jfx.tools.ScientificDoubleStringConverter;
import us.ihmc.scs2.sessionVisualizer.jfx.yoComposite.YoComposite;
import us.ihmc.scs2.sharedMemory.LinkedYoRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// FIXME Need to manually do some cleanup when the cell is being updated.
public class YoCompositeListCell extends ListCell<YoComposite>
{
   // TODO Need to make the controls resizable
   private static final double GRAPHIC_PREF_WIDTH = 110.0;

   private final YoManager yoManager;
   private final ListView<YoComposite> owner;

   private final ReadOnlyProperty<YoNameDisplay> nameDisplay;

   private YoComposite yoComposite;
   private Labeled yoCompositeNameDisplay = this;
   private final Property<Integer> numberPrecision;

   private final List<YoVariableProperty<?, ?>> yoVariableProperties = new ArrayList<>();

   public YoCompositeListCell(YoManager yoManager, ReadOnlyProperty<YoNameDisplay> nameDisplay, Property<Integer> numberPrecision, ListView<YoComposite> owner)
   {
      this.yoManager = yoManager;
      this.nameDisplay = nameDisplay;
      this.numberPrecision = numberPrecision;
      this.owner = owner;
      getStyleClass().add("yo-variable-list-cell");
   }

   @Override
   protected void updateItem(YoComposite yoComposite, boolean empty)
   {
      this.yoComposite = yoComposite;
      super.updateItem(yoComposite, empty);

      // Cleanup the properties: remove listeners and disable linked buffer
      yoVariableProperties.forEach(YoVariableProperty::dispose);
      yoVariableProperties.clear();

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
         YoVariable yoVariable = yoComposite.getYoComponents().get(0);

         Region yoVariableControl = createYoVariableControl(yoVariable, numberPrecision, yoManager.getLinkedRootRegistry());
         setGraphic(yoVariableControl);
         setContentDisplay(ContentDisplay.LEFT);
         setAlignment(Pos.CENTER_LEFT);
         setGraphicTextGap(5);
         yoCompositeNameDisplay = this;
      }
      else
      {
         List<Region> yoVariableControls = createYoVariableControls(yoComposite.getYoComponents(), numberPrecision, yoManager.getLinkedRootRegistry());

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

      updateYoCompositeName(nameDisplay.getValue());
      nameDisplay.addListener((o, oldValue, newValue) -> updateYoCompositeName(newValue));
      yoCompositeNameDisplay.setTooltip(new Tooltip(yoComposite.getName() + "\n" + yoComposite.getNamespace()));
   }

   private void updateYoCompositeName(YoNameDisplay nameDisplay)
   {
      if (yoCompositeNameDisplay == null || yoComposite == null)
         return;

      yoCompositeNameDisplay.setText(switch (nameDisplay)
                                     {
                                        case SHORT_NAME -> yoComposite.getName();
                                        case UNIQUE_NAME -> yoComposite.getUniqueName();
                                        case UNIQUE_SHORT_NAME -> yoComposite.getUniqueShortName();
                                        case FULL_NAME -> yoComposite.getFullname();
                                     });
   }

   public List<Region> createYoVariableControls(Collection<YoVariable> yoVariables, Property<Integer> numberPrecision, LinkedYoRegistry linkedRegistry)
   {
      return yoVariables.stream().map(v -> createYoVariableControl(v, numberPrecision, linkedRegistry)).collect(Collectors.toList());
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public Region createYoVariableControl(YoVariable yoVariable, Property<Integer> numberPrecision, LinkedYoRegistry linkedRegistry)
   {
      if (yoVariable instanceof YoDouble)
         return createYoDoubleControl((YoDouble) yoVariable, numberPrecision, linkedRegistry);
      if (yoVariable instanceof YoBoolean)
         return createYoBooleanControl((YoBoolean) yoVariable, linkedRegistry);
      if (yoVariable instanceof YoLong)
         return createYoLongControl((YoLong) yoVariable, linkedRegistry);
      if (yoVariable instanceof YoInteger)
         return createYoIntegerControl((YoInteger) yoVariable, linkedRegistry);
      if (yoVariable instanceof YoEnum)
         return createYoEnumControl((YoEnum) yoVariable, linkedRegistry);
      throw new UnsupportedOperationException("Unhandled YoVariable type: " + yoVariable.getClass().getSimpleName());
   }

   public Control createYoDoubleControl(YoDouble yoDouble, Property<Integer> numberPrecision, LinkedYoRegistry linkedRegistry)
   {
      YoDoubleProperty yoDoubleProperty = new YoDoubleProperty(yoDouble, this);
      yoDoubleProperty.setLinkedBuffer(isDisabled() ? null : linkedRegistry.linkYoVariable(yoDouble, yoDoubleProperty));
      disabledProperty().addListener((o, oldValue, newValue) -> yoDoubleProperty.setLinkedBuffer(newValue ?
                                                                                                       null :
                                                                                                       linkedRegistry.linkYoVariable(yoDouble,
                                                                                                                                     yoDoubleProperty)));
      yoVariableProperties.add(yoDoubleProperty);

      YoDoubleSpinnerValueFactory valueFactory = new YoDoubleSpinnerValueFactory(yoDoubleProperty.getValue());
      DoubleStringConverter rawDoubleStringConverter = new DoubleStringConverter();
      ScientificDoubleStringConverter scientificDoubleStringConverter = new ScientificDoubleStringConverter(numberPrecision);
      valueFactory.setConverter(scientificDoubleStringConverter);
      Spinner<Double> spinner = new Spinner<>(valueFactory);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      spinner.focusedProperty().addListener((o, oldValue, newValue) ->
                                            {
                                               valueFactory.setConverter(newValue ? rawDoubleStringConverter : scientificDoubleStringConverter);

                                               // When gaining focus: we want to update the text to reflect the change of the converter.
                                               // When losing focus: the text may be inconsistent with the actual value: reset the text.
                                               spinner.getEditor().setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                                            });
      yoDoubleProperty.bindDoubleProperty(spinner.getValueFactory().valueProperty());

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public Region createYoBooleanControl(YoBoolean yoBoolean, LinkedYoRegistry linkedRegistry)
   {
      YoBooleanProperty yoBooleanProperty = new YoBooleanProperty(yoBoolean, this);
      yoBooleanProperty.setLinkedBuffer(isDisabled() ? null : linkedRegistry.linkYoVariable(yoBoolean, yoBooleanProperty));
      disabledProperty().addListener((o, oldValue, newValue) -> yoBooleanProperty.setLinkedBuffer(newValue ?
                                                                                                        null :
                                                                                                        linkedRegistry.linkYoVariable(yoBoolean,
                                                                                                                                      yoBooleanProperty)));
      yoVariableProperties.add(yoBooleanProperty);

      CheckBox checkBox = new CheckBox();
      HBox root = new HBox(checkBox);
      root.setPrefWidth(GRAPHIC_PREF_WIDTH);
      root.alignmentProperty().set(Pos.CENTER_LEFT);
      checkBox.setSelected(yoBooleanProperty.getValue());
      yoBooleanProperty.bindBooleanProperty(checkBox.selectedProperty());

      return root;
   }

   public Control createYoLongControl(YoLong yoLong, LinkedYoRegistry linkedRegistry)
   {
      YoLongProperty yoLongProperty = new YoLongProperty(yoLong, this);
      yoLongProperty.setLinkedBuffer(isDisabled() ? null : linkedRegistry.linkYoVariable(yoLong, yoLongProperty));
      disabledProperty().addListener((o, oldValue, newValue) -> yoLongProperty.setLinkedBuffer(newValue ?
                                                                                                     null :
                                                                                                     linkedRegistry.linkYoVariable(yoLong, yoLongProperty)));
      yoVariableProperties.add(yoLongProperty);

      LongSpinnerValueFactory valueFactory = new LongSpinnerValueFactory(Long.MIN_VALUE, Long.MAX_VALUE, yoLongProperty.getValue(), 1L);
      Spinner<Long> spinner = new Spinner<>(valueFactory);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      spinner.focusedProperty().addListener((o, oldValue, newValue) ->
                                            {
                                               if (!newValue)
                                               { // Losing focus
                                                  // Workaround: manually reset to the current value
                                                  spinner.getEditor().setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                                               }
                                            });
      yoLongProperty.bindLongProperty(spinner.getValueFactory().valueProperty());

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public Control createYoIntegerControl(YoInteger yoInteger, LinkedYoRegistry linkedRegistry)
   {
      YoIntegerProperty yoIntegerProperty = new YoIntegerProperty(yoInteger, this);
      yoIntegerProperty.setLinkedBuffer(isDisabled() ? null : linkedRegistry.linkYoVariable(yoInteger, yoIntegerProperty));
      disabledProperty().addListener((o, oldValue, newValue) -> yoIntegerProperty.setLinkedBuffer(newValue ?
                                                                                                        null :
                                                                                                        linkedRegistry.linkYoVariable(yoInteger,
                                                                                                                                      yoIntegerProperty)));
      yoVariableProperties.add(yoIntegerProperty);

      IntegerSpinnerValueFactory valueFactory = new IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, yoIntegerProperty.getValue(), 1);
      Spinner<Integer> spinner = new Spinner<>(valueFactory);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      spinner.focusedProperty().addListener((o, oldValue, newValue) ->
                                            {
                                               if (!newValue)
                                               { // Losing focus
                                                  // Workaround: manually reset to the current value
                                                  spinner.getEditor().setText(valueFactory.getConverter().toString(valueFactory.getValue()));
                                               }
                                            });
      yoIntegerProperty.bindIntegerProperty(spinner.getValueFactory().valueProperty());

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.setTooltip(tooltip);

      return spinner;
   }

   public <E extends Enum<E>> Control createYoEnumControl(YoEnum<E> yoEnum, LinkedYoRegistry linkedRegistry)
   {
      YoEnumAsStringProperty<E> yoEnumProperty = new YoEnumAsStringProperty<>(yoEnum, this);
      yoEnumProperty.setLinkedBuffer(isDisabled() ? null : linkedRegistry.linkYoVariable(yoEnum, yoEnumProperty));
      disabledProperty().addListener((o, oldValue, newValue) -> yoEnumProperty.setLinkedBuffer(newValue ?
                                                                                                     null :
                                                                                                     linkedRegistry.linkYoVariable(yoEnum, yoEnumProperty)));
      yoVariableProperties.add(yoEnumProperty);

      ObservableList<String> items = FXCollections.observableArrayList(yoEnumProperty.getYoVariable().getEnumValuesAsString());
      if (yoEnumProperty.getYoVariable().isNullAllowed())
         items.add(YoEnum.NULL_VALUE_STRING);
      ComboBox<String> comboBox = new ComboBox<>(items);
      comboBox.setValue(yoEnumProperty.getValue());
      comboBox.setPrefWidth(GRAPHIC_PREF_WIDTH);
      comboBox.setEditable(false);
      yoEnumProperty.bindStringProperty(comboBox.valueProperty());

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(comboBox.valueProperty());
      comboBox.setTooltip(tooltip);

      return comboBox;
   }
}