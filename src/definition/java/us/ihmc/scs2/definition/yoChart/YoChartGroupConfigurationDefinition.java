package us.ihmc.scs2.definition.yoChart;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "YoChartGroupConfiguration")
public class YoChartGroupConfigurationDefinition
{
   private int numberOfRows, numberOfColumns;
   private List<YoChartConfigurationDefinition> chartConfigurations;

   @XmlElement
   public void setNumberOfRows(int numberOfRows)
   {
      this.numberOfRows = numberOfRows;
   }

   @XmlElement
   public void setNumberOfColumns(int numberOfColumns)
   {
      this.numberOfColumns = numberOfColumns;
   }

   @XmlElement
   public void setChartConfigurations(List<YoChartConfigurationDefinition> chartConfigurations)
   {
      this.chartConfigurations = chartConfigurations;
   }

   public int getNumberOfRows()
   {
      return numberOfRows;
   }

   public int getNumberOfColumns()
   {
      return numberOfColumns;
   }

   public List<YoChartConfigurationDefinition> getChartConfigurations()
   {
      return chartConfigurations;
   }
}
