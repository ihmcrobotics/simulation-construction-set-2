package us.ihmc.scs2.definition.robot;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.ejml.data.DMatrix;

import us.ihmc.euclid.matrix.interfaces.Matrix3DBasics;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;

@XmlType(propOrder = {"ixx", "iyy", "izz", "ixy", "ixz", "iyz"})
public class MomentOfInertiaDefinition implements Matrix3DBasics
{
   private double ixx, iyy, izz;
   private double ixy, ixz, iyz;

   public MomentOfInertiaDefinition()
   {
   }

   public MomentOfInertiaDefinition(double ixx, double iyy, double izz)
   {
      setPrincipalComponents(ixx, iyy, izz);
   }

   public MomentOfInertiaDefinition(double ixx, double iyy, double izz, double ixy, double ixz, double iyz)
   {
      setPrincipalComponents(ixx, iyy, izz);
      setCrossComponents(ixy, ixz, iyz);
   }

   public MomentOfInertiaDefinition(Matrix3DReadOnly matrix3D)
   {
      set(matrix3D);
   }

   public MomentOfInertiaDefinition(DMatrix matrix)
   {
      set(matrix);
   }

   public void setPrincipalComponents(double ixx, double iyy, double izz)
   {
      setIxx(ixx);
      setIyy(iyy);
      setIzz(izz);
   }

   public void setCrossComponents(double ixy, double ixz, double iyz)
   {
      setIxy(ixy);
      setIxz(ixz);
      setIyz(iyz);
   }

   @Override
   public void add(Matrix3DReadOnly other)
   {
      addM00(other.getM00());
      addM11(other.getM11());
      addM22(other.getM22());

      addM01(other.getM01());
      addM02(other.getM02());
      addM12(other.getM12());
   }

   @Override
   public void add(Matrix3DReadOnly matrix1, Matrix3DReadOnly matrix2)
   {
      setIxx(matrix1.getM00() + matrix2.getM00());
      setIyy(matrix1.getM11() + matrix2.getM11());
      setIzz(matrix1.getM22() + matrix2.getM22());

      setIxy(matrix1.getM01() + matrix2.getM01());
      setIxz(matrix1.getM02() + matrix2.getM02());
      setIyz(matrix1.getM12() + matrix2.getM12());
   }

   @XmlAttribute
   public void setIxx(double ixx)
   {
      this.ixx = ixx;
   }

   @XmlAttribute
   public void setIyy(double iyy)
   {
      this.iyy = iyy;
   }

   @XmlAttribute
   public void setIzz(double izz)
   {
      this.izz = izz;
   }

   @XmlAttribute
   public void setIxy(double ixy)
   {
      this.ixy = ixy;
   }

   @XmlAttribute
   public void setIxz(double ixz)
   {
      this.ixz = ixz;
   }

   @XmlAttribute
   public void setIyz(double iyz)
   {
      this.iyz = iyz;
   }

   public double getIxx()
   {
      return ixx;
   }

   public double getIyy()
   {
      return iyy;
   }

   public double getIzz()
   {
      return izz;
   }

   public double getIxy()
   {
      return ixy;
   }

   public double getIxz()
   {
      return ixz;
   }

   public double getIyz()
   {
      return iyz;
   }

   @XmlTransient
   @Override
   public void setM00(double m00)
   {
      setIxx(m00);
   }

   @XmlTransient
   @Override
   public void setM01(double m01)
   {
      setIxy(m01);
   }

   @XmlTransient
   @Override
   public void setM02(double m02)
   {
      setIxz(m02);
   }

   @XmlTransient
   @Override
   public void setM10(double m10)
   {
      setIxy(m10);
   }

   @XmlTransient
   @Override
   public void setM11(double m11)
   {
      setIyy(m11);
   }

   @XmlTransient
   @Override
   public void setM12(double m12)
   {
      setIyz(m12);
   }

   @XmlTransient
   @Override
   public void setM20(double m20)
   {
      setIxz(m20);
   }

   @XmlTransient
   @Override
   public void setM21(double m21)
   {
      setIyz(m21);
   }

   @XmlTransient
   @Override
   public void setM22(double m22)
   {
      setIzz(m22);
   }

   @Override
   public double getM00()
   {
      return getIxx();
   }

   @Override
   public double getM01()
   {
      return getIxy();
   }

   @Override
   public double getM02()
   {
      return getIxz();
   }

   @Override
   public double getM10()
   {
      return getIxy();
   }

   @Override
   public double getM11()
   {
      return getIyy();
   }

   @Override
   public double getM12()
   {
      return getIyz();
   }

   @Override
   public double getM20()
   {
      return getIxz();
   }

   @Override
   public double getM21()
   {
      return getIyz();
   }

   @Override
   public double getM22()
   {
      return getIzz();
   }
}
