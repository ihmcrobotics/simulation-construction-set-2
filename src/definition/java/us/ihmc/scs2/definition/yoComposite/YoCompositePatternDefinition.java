package us.ihmc.scs2.definition.yoComposite;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import us.ihmc.scs2.definition.yoChart.YoChartGroupModelDefinition;

@XmlRootElement(name = "YoCompositePattern")
public class YoCompositePatternDefinition
{
   private String name;
   private boolean crossRegistry = false;
   private final List<String> identifiers = new ArrayList<>();
   private final List<YoChartGroupModelDefinition> preferredConfigurations = new ArrayList<>();

   public YoCompositePatternDefinition()
   {
   }

   public YoCompositePatternDefinition(String name)
   {
      setName(name);
   }

   public YoCompositePatternDefinition(YoCompositePatternDefinition other)
   {
      if (other == null)
         return;

      setName(other.name);
      setCrossRegistry(other.crossRegistry);
      setIdentifiers(other.identifiers);
      setPreferredConfigurations(other.preferredConfigurations);
   }

   @XmlAttribute
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlElement
   public void setCrossRegistry(boolean crossRegistry)
   {
      this.crossRegistry = crossRegistry;
   }

   @XmlElement
   public void setIdentifiers(List<String> identifiers)
   {
      this.identifiers.clear();
      this.identifiers.addAll(identifiers);
   }

   @XmlElement
   public void setPreferredConfigurations(List<YoChartGroupModelDefinition> preferredConfigurations)
   {
      this.preferredConfigurations.clear();
      for (YoChartGroupModelDefinition model : preferredConfigurations)
         this.preferredConfigurations.add(model.clone());
   }

   public String getName()
   {
      return name;
   }

   public boolean isCrossRegistry()
   {
      return crossRegistry;
   }

   public List<String> getIdentifiers()
   {
      return identifiers;
   }

   public List<YoChartGroupModelDefinition> getPreferredConfigurations()
   {
      return preferredConfigurations;
   }

   @Override
   public YoCompositePatternDefinition clone()
   {
      return new YoCompositePatternDefinition(this);
   }

   @Override
   public boolean equals(Object object)
   {
      if (object == this)
      {
         return true;
      }
      else if (object instanceof YoCompositePatternDefinition)
      {
         YoCompositePatternDefinition other = (YoCompositePatternDefinition) object;
         if (name == null ? other.name != null : !name.equals(other.name))
            return false;
         if (crossRegistry != other.crossRegistry)
            return false;
         if (!identifiers.equals(other.identifiers))
            return false;
         if (!preferredConfigurations.equals(other.preferredConfigurations))
            return false;
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return "name: " + name + ", ids: " + identifiers.toString() + ", chart ids: " + preferredConfigurations.toString();
   }
}
