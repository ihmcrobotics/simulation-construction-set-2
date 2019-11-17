package us.ihmc.scs2.sessionVisualizer.properties;

import javafx.beans.property.Property;
import us.ihmc.yoVariables.variable.YoVariable;

public interface YoVariableProperty<T extends YoVariable<T>, P> extends Property<P>
{
   Property<P> userInputProperty();

   T getYoVariable();
}
