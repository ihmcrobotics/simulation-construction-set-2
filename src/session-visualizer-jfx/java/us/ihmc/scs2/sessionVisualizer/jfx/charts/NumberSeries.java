package us.ihmc.scs2.sessionVisualizer.jfx.charts;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NumberSeries
{
   /** The user displayable name for this series */
   private final StringProperty seriesNameProperty = new SimpleStringProperty(this, "seriesName", null);
   private final StringProperty currentValueProperty = new SimpleStringProperty(this, "currentValue", null);
   private final ObjectProperty<DataEntry> dataEntryProperty = new SimpleObjectProperty<>(this, "dataEntry", null);
   private final ObjectProperty<ChartIntegerBounds> dataXBoundsProperty = new SimpleObjectProperty<>(this, "dataXBounds", null);
   private final ObjectProperty<ChartDoubleBounds> dataYBoundsProperty = new SimpleObjectProperty<>(this, "dataYBounds", null);

   // User properties
   private final BooleanProperty negatedProperty = new SimpleBooleanProperty(this, "negated", false);
   private final ObjectProperty<ChartDoubleBounds> customYBoundsProperty = new SimpleObjectProperty<>(this, "customYBounds", null);

   public NumberSeries(String name)
   {
      setSeriesName(name);
      dataEntryProperty.addListener((o, oldValue, newValue) ->
      {
         dataXBoundsProperty.set(newValue != null ? newValue.getXBounds() : null);
         dataYBoundsProperty.set(newValue != null ? newValue.getYBounds() : null);
      });
   }

   public final String getSeriesName()
   {
      return seriesNameProperty.get();
   }

   public final void setSeriesName(String value)
   {
      seriesNameProperty.set(value);
   }

   public final StringProperty seriesNameProperty()
   {
      return seriesNameProperty;
   }

   public final void setCurrentValue(String value)
   {
      currentValueProperty.set(value);
   }

   public final String getCurrentValue()
   {
      return currentValueProperty.get();
   }

   public final StringProperty currentValueProperty()
   {
      return currentValueProperty;
   }

   public final DataEntry getDataEntry()
   {
      return dataEntryProperty.getValue();
   }

   public final void setDataEntry(DataEntry value)
   {
      dataEntryProperty.setValue(value);
   }

   public final ObjectProperty<DataEntry> dataEntryProperty()
   {
      return dataEntryProperty;
   }

   public final ChartIntegerBounds getDataXBounds()
   {
      return dataXBoundsProperty.get();
   }

   public final ReadOnlyObjectProperty<ChartIntegerBounds> dataXBoundsProperty()
   {
      return dataXBoundsProperty;
   }

   public final ChartDoubleBounds getDataYBounds()
   {
      return dataYBoundsProperty.get();
   }

   public final ReadOnlyObjectProperty<ChartDoubleBounds> dataYBoundsProperty()
   {
      return dataYBoundsProperty;
   }

   public final boolean isNegated()
   {
      return negatedProperty.get();
   }

   public final void setNegated(boolean value)
   {
      negatedProperty.set(value);
   }

   public final BooleanProperty negatedProperty()
   {
      return negatedProperty;
   }

   public final ChartDoubleBounds getCustomYBounds()
   {
      return customYBoundsProperty.get();
   }

   public final void setCustomYBounds(ChartDoubleBounds customYBounds)
   {
      customYBoundsProperty.set(customYBounds);
   }

   public ObjectProperty<ChartDoubleBounds> customYBoundsProperty()
   {
      return customYBoundsProperty;
   }
}