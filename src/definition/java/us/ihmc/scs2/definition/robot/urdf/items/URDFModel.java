package us.ihmc.scs2.definition.robot.urdf.items;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="robot")
public class URDFModel implements URDFItem
{
   private String name;
   private List<URDFMaterial> materials;
   private List<URDFLink> links;
   private List<URDFJoint> joints;

   @XmlAttribute(name = "name")
   public void setName(String name)
   {
      this.name = name;
   }

   @XmlElement(name = "material")
   public void setMaterials(List<URDFMaterial> materials)
   {
      this.materials = materials;
   }

   @XmlElement(name = "link")
   public void setLinks(List<URDFLink> links)
   {
      this.links = links;
   }

   @XmlElement(name = "joint")
   public void setJoints(List<URDFJoint> joints)
   {
      this.joints = joints;
   }

   public String getName()
   {
      return name;
   }

   public List<URDFMaterial> getMaterials()
   {
      return materials;
   }

   public List<URDFLink> getLinks()
   {
      return links;
   }

   public List<URDFJoint> getJoints()
   {
      return joints;
   }

   @Override
   public String getContentAsString()
   {
      return format("[name: %s, materials: %s, links: %s, joints: %s]", name, materials, links, joints);
   }

   @Override
   public String toString()
   {
      return itemToString();
   }

   @Override
   public List<URDFFilenameHolder> getFilenameHolders()
   {
      return URDFItem.combineItemListsFilenameHolders(materials, links, joints);
   }
}
