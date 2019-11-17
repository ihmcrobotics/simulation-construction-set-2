package us.ihmc.scs2.sessionVisualizer.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import us.ihmc.scs2.sessionVisualizer.yoComposite.YoComposite;
import us.ihmc.yoVariables.registry.NameSpace;

public class YoCompositeTest
{

   @Test
   public void testComputeUniqueNames()
   {
      int numberOfVariables = 21;
      NameSpace[] nameSpaces = new NameSpace[numberOfVariables];
      int i = 0;
      nameSpaces[i++] = new NameSpace("short.caramel");
      nameSpaces[i++] = new NameSpace("un.miam.caramel");
      nameSpaces[i++] = new NameSpace("deux.miam.caramel");
      nameSpaces[i++] = new NameSpace("trois.different.caramel");
      nameSpaces[i++] = new NameSpace("quatre.yummy.caramel");
      nameSpaces[i++] = new NameSpace("cinq.choup.caramel");
      nameSpaces[i++] = new NameSpace("six.yummy.caramel");
      nameSpaces[i++] = new NameSpace("sept.mouais.caramel");
      nameSpaces[i++] = new NameSpace("huit.cest.cacao.chocolat");
      nameSpaces[i++] = new NameSpace("neuf.pas.cacao.chocolat");
      nameSpaces[i++] = new NameSpace("dix.pareil.cafe.chocolat");
      nameSpaces[i++] = new NameSpace("onze.la.cafe.chocolat");
      nameSpaces[i++] = new NameSpace("douze.onsenfoula.demisel.beurre");
      nameSpaces[i++] = new NameSpace("treize.onsenfoula.nature.beurre");
      nameSpaces[i++] = new NameSpace("quatorze.quelquechose.nappe.table.sel");
      nameSpaces[i++] = new NameSpace("quinze.quelquechose.poivre.table.sel");
      nameSpaces[i++] = new NameSpace("seize.toi.maison.chat.miaou");
      nameSpaces[i++] = new NameSpace("dixsept.moi.maison.chat.miaou");
      nameSpaces[i++] = new NameSpace("dixhuit.serpent");
      nameSpaces[i++] = new NameSpace("dixneuf.giraffe");
      nameSpaces[i++] = new NameSpace("vingt.chat");

      String[] varNames = new String[numberOfVariables];

      for (int j = 0; j < numberOfVariables; j++)
      {
         NameSpace nameSpace = nameSpaces[j];
         varNames[j] = nameSpace.getShortName();
         List<String> subNames = nameSpace.getSubNames();
         nameSpaces[j] = new NameSpace(subNames.subList(0, subNames.size() - 1));
      }

      List<YoComposite> yoTypeReferences = new ArrayList<>();

      for (int j = 0; j < numberOfVariables; j++)
      {
         yoTypeReferences.add(new YoComposite(null, varNames[j], nameSpaces[j], null));
      }

      YoComposite.computeUniqueNames(yoTypeReferences);

      Set<String> uniqueNameSet = new HashSet<>();

      for (YoComposite yoTypeReference : yoTypeReferences)
      {
         assertTrue(uniqueNameSet.add(yoTypeReference.getUniqueName()));
      }

      i = 0;
      assertEquals("short.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("un.miam.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("deux.miam.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("different.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("quatre.yummy.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("choup.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("six.yummy.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("mouais.caramel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("cest.cacao.chocolat", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("pas.cacao.chocolat", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("pareil.cafe.chocolat", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("la.cafe.chocolat", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("demisel.beurre", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("nature.beurre", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("nappe.table.sel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("poivre.table.sel", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("toi.maison.chat.miaou", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("moi.maison.chat.miaou", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("serpent", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("giraffe", yoTypeReferences.get(i++).getUniqueName());
      assertEquals("chat", yoTypeReferences.get(i++).getUniqueName());
   }
}
