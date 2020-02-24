package us.ihmc.scs2.sessionVisualizer.charts;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.gsi.dataset.AxisDescription;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.DataSet2D;
import de.gsi.dataset.event.EventListener;
import de.gsi.dataset.event.UpdateEvent;
import de.gsi.dataset.locks.DataSetLock;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.ihmc.yoVariables.variable.YoVariable;

public class YoDoubleDataSet implements DataSet2D
{
   private static final long serialVersionUID = 1545454904627313514L;

   private final YoVariable<?> yoVariable;
   private final DoubleDataSet rawDataSet;
   private DataSet2D outputDataSet;

   private final BooleanProperty negatedProperty = new SimpleBooleanProperty(this, "negatedProperty", false);
   private final BooleanProperty normalizedProperty = new SimpleBooleanProperty(this, "normalizedProperty", false);

   private final ObjectProperty<ChartDoubleBounds> customYBoundsProperty = new SimpleObjectProperty<>(this, "customYBounds", null);
   private final ObjectProperty<ChartDoubleBounds> dataYBoundsProperty = new SimpleObjectProperty<>(this, "customYBounds", null);

   private double xMin, xMax, yMin, yMax;

   public YoDoubleDataSet(YoVariable<?> yoVariable, int initialSize)
   {
      this.yoVariable = yoVariable;

      rawDataSet = new DoubleDataSet(yoVariable.getName(), initialSize)
      {
         private static final long serialVersionUID = -8402193494986773179L;

         @Override
         public DoubleDataSet recomputeLimits(int dimension)
         {
            if (dimension == DIM_X)
            {
               AxisDescription xAxisDescription = rawDataSet.getAxisDescription(0);
               xAxisDescription.clear();
               xAxisDescription.set(xMin, xMax);
            }
            else
            {
               if (dimension == DIM_Y)
               {
                  AxisDescription yAxisDescription = rawDataSet.getAxisDescription(1);
                  yAxisDescription.clear();

                  ChartDoubleBounds yBounds = customYBoundsProperty.get();
                  if (yBounds == null)
                     yAxisDescription.set(yMin, yMax);
                  else
                     yAxisDescription.set(yBounds.getLower(), yBounds.getUpper());
               }
            }
            return this;
         }
      };
      outputDataSet = rawDataSet;

      customYBoundsProperty.addListener((o, oldValue, newValue) -> getAxisDescriptions().forEach(AxisDescription::clear));

      negatedProperty.addListener((observable, oldValue, newValue) ->
      {
         if (newValue)
         {
            if (normalizedProperty.get())
               outputDataSet = new YNormalizedDataSetWrapper(new YNegatedDataSetWrapper(rawDataSet));
            else
               outputDataSet = new YNegatedDataSetWrapper(rawDataSet);
         }
         else
         {
            if (normalizedProperty.get())
               outputDataSet = new YNormalizedDataSetWrapper(rawDataSet);
            else
               outputDataSet = rawDataSet;
         }
         fireInvalidated(new UpdateEvent(rawDataSet, "YoDoubleDataSet - swapped output"));
      });

      normalizedProperty.addListener((observable, oldValue, newValue) ->
      {
         if (newValue)
         {
            if (negatedProperty.get())
               outputDataSet = new YNormalizedDataSetWrapper(new YNegatedDataSetWrapper(rawDataSet));
            else
               outputDataSet = new YNormalizedDataSetWrapper(rawDataSet);
         }
         else
         {
            if (negatedProperty.get())
               outputDataSet = new YNegatedDataSetWrapper(rawDataSet);
            else
               outputDataSet = rawDataSet;
         }
         fireInvalidated(new UpdateEvent(rawDataSet, "YoDoubleDataSet - swapped output"));
      });
   }

   public void setRange(double xMin, double xMax, double yMin, double yMax)
   {
      this.xMin = xMin;
      this.xMax = xMax;
      this.yMin = yMin;
      this.yMax = yMax;
      dataYBoundsProperty.set(new ChartDoubleBounds(yMin, yMax));
   }

   public YoVariable<?> getYoVariable()
   {
      return yoVariable;
   }

   public DoubleDataSet getRawDataSet()
   {
      return rawDataSet;
   }

   public BooleanProperty negatedProperty()
   {
      return negatedProperty;
   }

   public void setNegated(boolean negate)
   {
      negatedProperty.set(negate);
   }

   public boolean isNegated()
   {
      return negatedProperty.get();
   }

   public BooleanProperty normalizedProperty()
   {
      return normalizedProperty;
   }

   public void setNormalized(boolean normalize)
   {
      normalizedProperty.set(normalize);
   }

   public boolean isNormalized()
   {
      return normalizedProperty.get();
   }

   public ObjectProperty<ChartDoubleBounds> dataYBoundsProperty()
   {
      return dataYBoundsProperty;
   }

   public void setDataYBounds(ChartDoubleBounds yBounds)
   {
      dataYBoundsProperty.set(yBounds);
   }

   public ChartDoubleBounds getDataYBounds()
   {
      return dataYBoundsProperty.get();
   }

   public ObjectProperty<ChartDoubleBounds> customYBoundsProperty()
   {
      return customYBoundsProperty;
   }

   public void setCustomYBounds(ChartDoubleBounds yBounds)
   {
      customYBoundsProperty.set(yBounds);
   }

   public ChartDoubleBounds getCustomYBounds()
   {
      return customYBoundsProperty.get();
   }

   // DataSet2D Methods

   @Override
   public double get(int dimIndex, int index)
   {
      return outputDataSet.get(dimIndex, index);
   }

   @Override
   public List<AxisDescription> getAxisDescriptions()
   {
      return outputDataSet.getAxisDescriptions();
   }

   @Override
   public int getIndex(int dimIndex, double value)
   {
      return outputDataSet.getIndex(dimIndex, value);
   }

   @Override
   public YoDoubleDataSet recomputeLimits(int dimension)
   {
      outputDataSet.recomputeLimits(dimension);
      return this;
   }

   @Override
   public AtomicBoolean autoNotification()
   {
      return rawDataSet.autoNotification();
   }

   @Override
   public List<EventListener> updateEventListener()
   {
      return rawDataSet.updateEventListener();
   }

   @Override
   public int getDataCount()
   {
      return rawDataSet.getDataCount();
   }

   @Override
   public String getDataLabel(int index)
   {
      return rawDataSet.getDataLabel(index);
   }

   @Override
   public int getDimension()
   {
      return rawDataSet.getDimension();
   }

   @Override
   public String getName()
   {
      return yoVariable.getName();
   }

   @Override
   public String getStyle()
   {
      return rawDataSet.getStyle();
   }

   @Override
   public String getStyle(int index)
   {
      return rawDataSet.getStyle(index);
   }

   /**
    * Notifies listeners that the data has been invalidated. If the data is added to the chart, it
    * triggers repaint.
    *
    * @param event the change event
    * @return itself (fluent design)
    */
   public YoDoubleDataSet fireInvalidated(final UpdateEvent event)
   {
      rawDataSet.invokeListener(event);
      return this;
   }

   @Override
   public YoDoubleDataSet setStyle(String style)
   {
      rawDataSet.setStyle(style);
      return this;
   }

   @SuppressWarnings("unchecked")
   @Override
   public DataSetLock<? extends DataSet> lock()
   {
      return rawDataSet.lock();
   }
}
