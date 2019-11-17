package us.ihmc.scs2.sessionVisualizer.controllers.yoComposite.search;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.javafx.binding.BidirectionalBinding;

import javafx.beans.InvalidationListener;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import us.ihmc.javaFXExtensions.control.LongSpinnerValueFactory;
import us.ihmc.javaFXExtensions.control.UnboundedDoubleSpinnerValueFactory;
import us.ihmc.scs2.sessionVisualizer.managers.YoManager;
import us.ihmc.scs2.sessionVisualizer.properties.YoBooleanProperty;
import us.ihmc.scs2.sessionVisualizer.properties.YoDoubleProperty;
import us.ihmc.scs2.sessionVisualizer.properties.YoEnumAsStringProperty;
import us.ihmc.scs2.sessionVisualizer.properties.YoIntegerProperty;
import us.ihmc.scs2.sessionVisualizer.properties.YoLongProperty;
import us.ihmc.scs2.sessionVisualizer.tools.ScientificDoubleStringConverter;
import us.ihmc.scs2.sessionVisualizer.yoComposite.YoComposite;
import us.ihmc.scs2.sharedMemory.LinkedYoVariableRegistry;
import us.ihmc.yoVariables.variable.YoBoolean;
import us.ihmc.yoVariables.variable.YoDouble;
import us.ihmc.yoVariables.variable.YoEnum;
import us.ihmc.yoVariables.variable.YoInteger;
import us.ihmc.yoVariables.variable.YoLong;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoCompositeListCell extends ListCell<YoComposite>
{
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
         YoVariable<?> yoVariable = YoVariable.class.cast(yoComposite.getYoComponents().get(0));

         Control yoVariableControl = createYoVariableControl(yoVariable, yoManager.getLinkedRootRegistry());
         setGraphic(yoVariableControl);
         setContentDisplay(ContentDisplay.LEFT);
         setAlignment(Pos.CENTER_LEFT);
         setGraphicTextGap(5);
         yoCompositeNameDisplay = this;
      }
      else
      {
         List<Control> yoVariableControls = createYoVariableControls(yoComposite.getYoComponents(), yoManager.getLinkedRootRegistry());

         GridPane cellGraphic = new GridPane();
         cellGraphic.setHgap(5.0);
         cellGraphic.setVgap(2.0);

         for (int i = 0; i < yoVariableControls.size(); i++)
         {
            YoVariable<?> component = yoComposite.getYoComponents().get(i);
            String componentIdentifier = yoComposite.getPattern().getComponentIdentifiers()[i];
            Label idLabel = new Label(componentIdentifier);
            idLabel.setTooltip(new Tooltip(component.getName() + "\n" + component.getNameSpace()));
            Control componentControl = yoVariableControls.get(i);
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

   public static List<Control> createYoVariableControls(Collection<YoVariable<?>> yoVariables, LinkedYoVariableRegistry linkedRegistry)
   {
      return yoVariables.stream().map(v -> createYoVariableControl(v, linkedRegistry)).collect(Collectors.toList());
   }

   public static Control createYoVariableControl(YoVariable<?> yoVariable, LinkedYoVariableRegistry linkedRegistry)
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

   public static Control createYoDoubleControl(YoDoubleProperty yoDoubleProperty, LinkedYoVariableRegistry linkedRegistry)
   {
      UnboundedDoubleSpinnerValueFactory valueFactory = new UnboundedDoubleSpinnerValueFactory(Double.NEGATIVE_INFINITY,
                                                                                               Double.POSITIVE_INFINITY,
                                                                                               yoDoubleProperty.getValue(),
                                                                                               yoDoubleProperty.getYoVariable().getStepSize());
      valueFactory.setConverter(new ScientificDoubleStringConverter(3));
      Spinner<Double> spinner = new Spinner<>(valueFactory);
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      BidirectionalBinding.bindNumber(yoDoubleProperty, spinner.getValueFactory().valueProperty());
      yoDoubleProperty.userInputProperty().addListener((InvalidationListener) observable ->
      {
         YoDouble yoDouble = yoDoubleProperty.getYoVariable();
         yoDouble.set(yoDoubleProperty.userInputProperty().get());
         linkedRegistry.push(yoDouble);
      });

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.getEditor().setTooltip(tooltip);

      return spinner;
   }

   public static Control createYoBooleanControl(YoBooleanProperty yoBooleanProperty, LinkedYoVariableRegistry linkedRegistry)
   {
      CheckBox checkBox = new CheckBox();
      checkBox.setPrefWidth(GRAPHIC_PREF_WIDTH);
      checkBox.setSelected(yoBooleanProperty.getValue());
      BidirectionalBinding.bind(yoBooleanProperty, checkBox.selectedProperty());
      yoBooleanProperty.userInputProperty().addListener((InvalidationListener) observable ->
      {
         YoBoolean yoBoolean = yoBooleanProperty.getYoVariable();
         yoBoolean.set(yoBooleanProperty.userInputProperty().get());
         linkedRegistry.push(yoBoolean);
      });
      return checkBox;
   }

   public static Control createYoLongControl(YoLongProperty yoLongProperty, LinkedYoVariableRegistry linkedRegistry)
   {
      Spinner<Long> spinner = new Spinner<>(new LongSpinnerValueFactory(Long.MIN_VALUE,
                                                                        Long.MAX_VALUE,
                                                                        yoLongProperty.getValue(),
                                                                        (long) yoLongProperty.getYoVariable().getStepSize()));
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      BidirectionalBinding.bindNumber(yoLongProperty, spinner.getValueFactory().valueProperty());
      yoLongProperty.userInputProperty().addListener((InvalidationListener) observable ->
      {
         YoLong yoLong = yoLongProperty.getYoVariable();
         yoLong.set(yoLongProperty.userInputProperty().get());
         linkedRegistry.push(yoLong);
      });

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.getEditor().setTooltip(tooltip);

      return spinner;
   }

   public static Control createYoIntegerControl(YoIntegerProperty yoIntegerProperty, LinkedYoVariableRegistry linkedRegistry)
   {
      Spinner<Integer> spinner = new Spinner<>(Integer.MIN_VALUE,
                                               Integer.MAX_VALUE,
                                               yoIntegerProperty.getValue(),
                                               (int) yoIntegerProperty.getYoVariable().getStepSize());
      spinner.setPrefWidth(GRAPHIC_PREF_WIDTH);
      spinner.setEditable(true);
      BidirectionalBinding.bindNumber(yoIntegerProperty, spinner.getValueFactory().valueProperty());
      yoIntegerProperty.userInputProperty().addListener((InvalidationListener) observable ->
      {
         YoInteger yoInteger = yoIntegerProperty.getYoVariable();
         yoInteger.set(yoIntegerProperty.userInputProperty().get());
         linkedRegistry.push(yoInteger);
      });

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(spinner.valueProperty().asString());
      spinner.getEditor().setTooltip(tooltip);

      return spinner;
   }

   public static <E extends Enum<E>> Control createYoEnumControl(YoEnumAsStringProperty<E> yoEnumProperty, LinkedYoVariableRegistry linkedRegistry)
   {
      ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(yoEnumProperty.getYoVariable().getEnumValuesAsString()));
      comboBox.setValue(yoEnumProperty.getValue());
      comboBox.setPrefWidth(GRAPHIC_PREF_WIDTH);
      comboBox.setEditable(false);
      BidirectionalBinding.bind(yoEnumProperty, comboBox.valueProperty());
      yoEnumProperty.userInputProperty().addListener((InvalidationListener) observable ->
      {
         YoEnum<E> yoEnum = yoEnumProperty.getYoVariable();
         yoEnum.set(yoEnumProperty.toEnumOrdinal(yoEnumProperty.userInputProperty().get()));
         linkedRegistry.push(yoEnum);
      });

      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(comboBox.valueProperty());
      comboBox.setTooltip(tooltip);

      return comboBox;
   }
}