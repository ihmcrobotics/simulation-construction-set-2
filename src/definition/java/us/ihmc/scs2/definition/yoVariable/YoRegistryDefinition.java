package us.ihmc.scs2.definition.yoVariable;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import us.ihmc.euclid.tools.EuclidCoreIOTools;

@XmlRootElement(name = "YoRegistry")
public class YoRegistryDefinition
{
   private String name;
   private List<YoVariableDefinition> yoVariables;
   private List<YoRegistryDefinition> yoRegistries;

   public YoRegistryDefinition()
   {
   }

   @XmlAttribute
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlElement
   public void setYoVariables(List<YoVariableDefinition> yoVariables)
   {
      this.yoVariables = yoVariables;
   }

   @XmlElement
   public void setYoRegistries(List<YoRegistryDefinition> yoRegistries)
   {
      this.yoRegistries = yoRegistries;
   }

   public String getName()
   {
      return name;
   }

   public List<YoVariableDefinition> getYoVariables()
   {
      return yoVariables;
   }

   public List<YoRegistryDefinition> getYoRegistries()
   {
      return yoRegistries;
   }

   @Override
   public String toString()
   {
      return "name: " + name + ", yoVariables: " + EuclidCoreIOTools.getCollectionString("[", "]", ", ", yoVariables, YoVariableDefinition::getName)
            + ", yoRegistries: " + EuclidCoreIOTools.getCollectionString("[", "]", ", ", yoRegistries, YoRegistryDefinition::getName);
   }
}
