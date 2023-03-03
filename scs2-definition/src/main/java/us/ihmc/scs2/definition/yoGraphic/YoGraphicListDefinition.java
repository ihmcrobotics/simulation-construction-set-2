package us.ihmc.scs2.definition.yoGraphic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "YoGraphicList")
public class YoGraphicListDefinition extends YoGraphicDefinition
{
   private List<YoGraphicDefinition> yoGraphics = new ArrayList<>();

   public YoGraphicListDefinition()
   {
      registerListField("yoGraphics", this::getYoGraphics, this::setYoGraphics, "g", YoGraphicDefinition::toParsableString, YoGraphicDefinition::parse);
   }

   public YoGraphicListDefinition(YoGraphicDefinition... yoGraphics)
   {
      this(Arrays.asList(yoGraphics));
   }

   public YoGraphicListDefinition(Collection<? extends YoGraphicDefinition> yoGraphics)
   {
      setYoGraphics(new ArrayList<>(yoGraphics));
   }

   public void clear()
   {
      if (yoGraphics != null)
         yoGraphics.clear();
   }

   public void addYoGraphic(YoGraphicDefinition yoGraphic)
   {
      if (yoGraphic == null)
         return;
      if (yoGraphic instanceof YoGraphicListDefinition list)
      {
         if (list.getYoGraphics() == null)
            return;
         for (int i = 0; i < list.getYoGraphics().size(); i++)
         {
            addYoGraphic(list.getYoGraphics().get(i));
         }
         return;
      }
      if (yoGraphics == null)
         yoGraphics = new ArrayList<>();
      yoGraphics.add(yoGraphic);
   }

   @XmlElement(name = "yoGraphic")
   public void setYoGraphics(List<YoGraphicDefinition> yoGraphics)
   {
      this.yoGraphics = yoGraphics;
   }

   public void unwrapNestedLists()
   {
      if (yoGraphics == null)
         return;

      for (int i = yoGraphics.size() - 1; i >= 0; i--)
      {
         YoGraphicDefinition yoGraphic = yoGraphics.get(i);

         if (yoGraphic instanceof YoGraphicListDefinition list)
         {
            yoGraphics.remove(i);
            if (list.getYoGraphics() != null)
            {
               list.unwrapNestedLists();
               yoGraphics.addAll(i, list.getYoGraphics());
            }
         }
      }
   }

   public List<YoGraphicDefinition> getYoGraphics()
   {
      return yoGraphics;
   }

   @Override
   public String toString(int indent)
   {
      String out = "%s [name=%s, visible=%b, yoGraphics=%s]";
      return out.formatted(getClass().getSimpleName(),
                           name,
                           visible,
                           indentedListString(indent, true, yoGraphics, yoGraphic -> yoGraphic.toString(indent + 1)));
   }
}
