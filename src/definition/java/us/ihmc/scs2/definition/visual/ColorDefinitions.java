package us.ihmc.scs2.definition.visual;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ColorDefinitions
{
   public static final Map<String, Supplier<ColorDefinition>> colorNameToSupplierMap;

   static
   {
      Map<String, Supplier<ColorDefinition>> map = Stream.of(ColorDefinitions.class.getDeclaredMethods())
                                                         .filter(m -> m.getReturnType() == ColorDefinition.class && Modifier.isPublic(m.getModifiers()))
                                                         .collect(Collectors.toMap(Method::getName, m -> () -> invokeMethod(m)));
      colorNameToSupplierMap = Collections.unmodifiableMap(map);
   }

   private static final ColorDefinition invokeMethod(Method method)
   {
      try
      {
         return (ColorDefinition) method.invoke(null);
      }
      catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
      {
         throw new RuntimeException("Problem invoking color factory", e);
      }
   }

   /**
    * The color alice blue with an RGB value of #F0F8FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F0F8FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition AliceBlue()
   {
      return new ColorDefinition(0.9411765, 0.972549, 1.0);
   }

   /**
    * The color antique white with an RGB value of #FAEBD7
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FAEBD7;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition AntiqueWhite()
   {
      return new ColorDefinition(0.98039216, 0.92156863, 0.84313726);
   }

   /**
    * The color aqua with an RGB value of #00FFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Aqua()
   {
      return new ColorDefinition(0.0, 1.0, 1.0);
   }

   /**
    * The color aquamarine with an RGB value of #7FFFD4
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#7FFFD4;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Aquamarine()
   {
      return new ColorDefinition(0.49803922, 1.0, 0.83137256);
   }

   /**
    * The color azure with an RGB value of #F0FFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F0FFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Azure()
   {
      return new ColorDefinition(0.9411765, 1.0, 1.0);
   }

   /**
    * The color beige with an RGB value of #F5F5DC
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F5F5DC;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Beige()
   {
      return new ColorDefinition(0.9607843, 0.9607843, 0.8627451);
   }

   /**
    * The color bisque with an RGB value of #FFE4C4
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFE4C4;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Bisque()
   {
      return new ColorDefinition(1.0, 0.89411765, 0.76862746);
   }

   /**
    * The color black with an RGB value of #000000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#000000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Black()
   {
      return new ColorDefinition(0.0, 0.0, 0.0);
   }

   /**
    * The color blanched almond with an RGB value of #FFEBCD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFEBCD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition BlanchedAlmond()
   {
      return new ColorDefinition(1.0, 0.92156863, 0.8039216);
   }

   /**
    * The color blue with an RGB value of #0000FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#0000FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Blue()
   {
      return new ColorDefinition(0.0, 0.0, 1.0);
   }

   /**
    * The color blue violet with an RGB value of #8A2BE2
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#8A2BE2;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition BlueViolet()
   {
      return new ColorDefinition(0.5411765, 0.16862746, 0.8862745);
   }

   /**
    * The color brown with an RGB value of #A52A2A
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#A52A2A;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Brown()
   {
      return new ColorDefinition(0.64705884, 0.16470589, 0.16470589);
   }

   /**
    * The color burly wood with an RGB value of #DEB887
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DEB887;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition BurlyWood()
   {
      return new ColorDefinition(0.87058824, 0.72156864, 0.5294118);
   }

   /**
    * The color cadet blue with an RGB value of #5F9EA0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#5F9EA0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition CadetBlue()
   {
      return new ColorDefinition(0.37254903, 0.61960787, 0.627451);
   }

   /**
    * The color chartreuse with an RGB value of #7FFF00
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#7FFF00;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Chartreuse()
   {
      return new ColorDefinition(0.49803922, 1.0, 0.0);
   }

   /**
    * The color chocolate with an RGB value of #D2691E
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#D2691E;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Chocolate()
   {
      return new ColorDefinition(0.8235294, 0.4117647, 0.11764706);
   }

   /**
    * The color coral with an RGB value of #FF7F50
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF7F50;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Coral()
   {
      return new ColorDefinition(1.0, 0.49803922, 0.3137255);
   }

   /**
    * The color cornflower blue with an RGB value of #6495ED
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#6495ED;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition CornflowerBlue()
   {
      return new ColorDefinition(0.39215687, 0.58431375, 0.92941177);
   }

   /**
    * The color cornsilk with an RGB value of #FFF8DC
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFF8DC;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Cornsilk()
   {
      return new ColorDefinition(1.0, 0.972549, 0.8627451);
   }

   /**
    * The color crimson with an RGB value of #DC143C
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DC143C;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Crimson()
   {
      return new ColorDefinition(0.8627451, 0.078431375, 0.23529412);
   }

   /**
    * The color cyan with an RGB value of #00FFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Cyan()
   {
      return new ColorDefinition(0.0, 1.0, 1.0);
   }

   /**
    * The color dark blue with an RGB value of #00008B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00008B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkBlue()
   {
      return new ColorDefinition(0.0, 0.0, 0.54509807);
   }

   /**
    * The color dark cyan with an RGB value of #008B8B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#008B8B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkCyan()
   {
      return new ColorDefinition(0.0, 0.54509807, 0.54509807);
   }

   /**
    * The color dark goldenrod with an RGB value of #B8860B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#B8860B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkGoldenrod()
   {
      return new ColorDefinition(0.72156864, 0.5254902, 0.043137256);
   }

   /**
    * The color dark gray with an RGB value of #A9A9A9
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkGray()
   {
      return new ColorDefinition(0.6627451, 0.6627451, 0.6627451);
   }

   /**
    * The color dark green with an RGB value of #006400
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#006400;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkGreen()
   {
      return new ColorDefinition(0.0, 0.39215687, 0.0);
   }

   /**
    * The color dark grey with an RGB value of #A9A9A9
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkGrey()
   {
      return DarkGray();
   }

   /**
    * The color dark khaki with an RGB value of #BDB76B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#BDB76B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkKhaki()
   {
      return new ColorDefinition(0.7411765, 0.7176471, 0.41960785);
   }

   /**
    * The color dark magenta with an RGB value of #8B008B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#8B008B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkMagenta()
   {
      return new ColorDefinition(0.54509807, 0.0, 0.54509807);
   }

   /**
    * The color dark olive green with an RGB value of #556B2F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#556B2F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkOliveGreen()
   {
      return new ColorDefinition(0.33333334, 0.41960785, 0.18431373);
   }

   /**
    * The color dark orange with an RGB value of #FF8C00
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF8C00;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkOrange()
   {
      return new ColorDefinition(1.0, 0.54901963, 0.0);
   }

   /**
    * The color dark orchid with an RGB value of #9932CC
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#9932CC;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkOrchid()
   {
      return new ColorDefinition(0.6, 0.19607843, 0.8);
   }

   /**
    * The color dark red with an RGB value of #8B0000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#8B0000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkRed()
   {
      return new ColorDefinition(0.54509807, 0.0, 0.0);
   }

   /**
    * The color dark salmon with an RGB value of #E9967A
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#E9967A;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkSalmon()
   {
      return new ColorDefinition(0.9137255, 0.5882353, 0.47843137);
   }

   /**
    * The color dark sea green with an RGB value of #8FBC8F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#8FBC8F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkSeaGreen()
   {
      return new ColorDefinition(0.56078434, 0.7372549, 0.56078434);
   }

   /**
    * The color dark slate blue with an RGB value of #483D8B
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#483D8B;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkSlateBlue()
   {
      return new ColorDefinition(0.28235295, 0.23921569, 0.54509807);
   }

   /**
    * The color dark slate gray with an RGB value of #2F4F4F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkSlateGray()
   {
      return new ColorDefinition(0.18431373, 0.30980393, 0.30980393);
   }

   /**
    * The color dark slate grey with an RGB value of #2F4F4F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkSlateGrey()
   {
      return DarkSlateGray();
   }

   /**
    * The color dark turquoise with an RGB value of #00CED1
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00CED1;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkTurquoise()
   {
      return new ColorDefinition(0.0, 0.80784315, 0.81960785);
   }

   /**
    * The color dark violet with an RGB value of #9400D3
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#9400D3;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DarkViolet()
   {
      return new ColorDefinition(0.5803922, 0.0, 0.827451);
   }

   /**
    * The color deep pink with an RGB value of #FF1493
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF1493;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DeepPink()
   {
      return new ColorDefinition(1.0, 0.078431375, 0.5764706);
   }

   /**
    * The color deep sky blue with an RGB value of #00BFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00BFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DeepSkyBlue()
   {
      return new ColorDefinition(0.0, 0.7490196, 1.0);
   }

   /**
    * The color dim gray with an RGB value of #696969
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DimGray()
   {
      return new ColorDefinition(0.4117647, 0.4117647, 0.4117647);
   }

   /**
    * The color dim grey with an RGB value of #696969
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DimGrey()
   {
      return DimGray();
   }

   /**
    * The color dodger blue with an RGB value of #1E90FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#1E90FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition DodgerBlue()
   {
      return new ColorDefinition(0.11764706, 0.5647059, 1.0);
   }

   /**
    * The color firebrick with an RGB value of #B22222
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#B22222;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition FireBrick()
   {
      return new ColorDefinition(0.69803923, 0.13333334, 0.13333334);
   }

   /**
    * The color floral white with an RGB value of #FFFAF0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFAF0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition FloralWhite()
   {
      return new ColorDefinition(1.0, 0.98039216, 0.9411765);
   }

   /**
    * The color forest green with an RGB value of #228B22
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#228B22;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition ForestGreen()
   {
      return new ColorDefinition(0.13333334, 0.54509807, 0.13333334);
   }

   /**
    * The color fuchsia with an RGB value of #FF00FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Fuchsia()
   {
      return new ColorDefinition(1.0, 0.0, 1.0);
   }

   /**
    * The color gainsboro with an RGB value of #DCDCDC
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DCDCDC;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Gainsboro()
   {
      return new ColorDefinition(0.8627451, 0.8627451, 0.8627451);
   }

   /**
    * The color ghost white with an RGB value of #F8F8FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F8F8FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition GhostWhite()
   {
      return new ColorDefinition(0.972549, 0.972549, 1.0);
   }

   /**
    * The color gold with an RGB value of #FFD700
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFD700;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Gold()
   {
      return new ColorDefinition(1.0, 0.84313726, 0.0);
   }

   /**
    * The color goldenrod with an RGB value of #DAA520
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DAA520;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Goldenrod()
   {
      return new ColorDefinition(0.85490197, 0.64705884, 0.1254902);
   }

   /**
    * The color gray with an RGB value of #808080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Gray()
   {
      return new ColorDefinition(0.5019608, 0.5019608, 0.5019608);
   }

   /**
    * The color green with an RGB value of #008000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#008000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Green()
   {
      return new ColorDefinition(0.0, 0.5019608, 0.0);
   }

   /**
    * The color green yellow with an RGB value of #ADFF2F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#ADFF2F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition GreenYellow()
   {
      return new ColorDefinition(0.6784314, 1.0, 0.18431373);
   }

   /**
    * The color grey with an RGB value of #808080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Grey()
   {
      return Gray();
   }

   /**
    * The color honeydew with an RGB value of #F0FFF0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F0FFF0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Honeydew()
   {
      return new ColorDefinition(0.9411765, 1.0, 0.9411765);
   }

   /**
    * The color hot pink with an RGB value of #FF69B4
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF69B4;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition HotPink()
   {
      return new ColorDefinition(1.0, 0.4117647, 0.7058824);
   }

   /**
    * The color indian red with an RGB value of #CD5C5C
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#CD5C5C;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition IndianRed()
   {
      return new ColorDefinition(0.8039216, 0.36078432, 0.36078432);
   }

   /**
    * The color indigo with an RGB value of #4B0082
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#4B0082;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Indigo()
   {
      return new ColorDefinition(0.29411766, 0.0, 0.50980395);
   }

   /**
    * The color ivory with an RGB value of #FFFFF0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFFF0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Ivory()
   {
      return new ColorDefinition(1.0, 1.0, 0.9411765);
   }

   /**
    * The color khaki with an RGB value of #F0E68C
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F0E68C;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Khaki()
   {
      return new ColorDefinition(0.9411765, 0.9019608, 0.54901963);
   }

   /**
    * The color lavender with an RGB value of #E6E6FA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#E6E6FA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Lavender()
   {
      return new ColorDefinition(0.9019608, 0.9019608, 0.98039216);
   }

   /**
    * The color lavender blush with an RGB value of #FFF0F5
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFF0F5;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LavenderBlush()
   {
      return new ColorDefinition(1.0, 0.9411765, 0.9607843);
   }

   /**
    * The color lawn green with an RGB value of #7CFC00
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#7CFC00;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LawnGreen()
   {
      return new ColorDefinition(0.4862745, 0.9882353, 0.0);
   }

   /**
    * The color lemon chiffon with an RGB value of #FFFACD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFACD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LemonChiffon()
   {
      return new ColorDefinition(1.0, 0.98039216, 0.8039216);
   }

   /**
    * The color light blue with an RGB value of #ADD8E6
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#ADD8E6;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightBlue()
   {
      return new ColorDefinition(0.6784314, 0.84705883, 0.9019608);
   }

   /**
    * The color light coral with an RGB value of #F08080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F08080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightCoral()
   {
      return new ColorDefinition(0.9411765, 0.5019608, 0.5019608);
   }

   /**
    * The color light cyan with an RGB value of #E0FFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#E0FFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightCyan()
   {
      return new ColorDefinition(0.8784314, 1.0, 1.0);
   }

   /**
    * The color light goldenrod yellow with an RGB value of #FAFAD2
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FAFAD2;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightGoldenrodYellow()
   {
      return new ColorDefinition(0.98039216, 0.98039216, 0.8235294);
   }

   /**
    * The color light gray with an RGB value of #D3D3D3
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightGray()
   {
      return new ColorDefinition(0.827451, 0.827451, 0.827451);
   }

   /**
    * The color light green with an RGB value of #90EE90
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#90EE90;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightGreen()
   {
      return new ColorDefinition(0.5647059, 0.93333334, 0.5647059);
   }

   /**
    * The color light grey with an RGB value of #D3D3D3
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightGrey()
   {
      return LightGray();
   }

   /**
    * The color light pink with an RGB value of #FFB6C1
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFB6C1;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightPink()
   {
      return new ColorDefinition(1.0, 0.7137255, 0.75686276);
   }

   /**
    * The color light salmon with an RGB value of #FFA07A
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFA07A;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSalmon()
   {
      return new ColorDefinition(1.0, 0.627451, 0.47843137);
   }

   /**
    * The color light sea green with an RGB value of #20B2AA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#20B2AA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSeaGreen()
   {
      return new ColorDefinition(0.1254902, 0.69803923, 0.6666667);
   }

   /**
    * The color light sky blue with an RGB value of #87CEFA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#87CEFA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSkyBlue()
   {
      return new ColorDefinition(0.5294118, 0.80784315, 0.98039216);
   }

   /**
    * The color light slate gray with an RGB value of #778899
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSlateGray()
   {
      return new ColorDefinition(0.46666667, 0.53333336, 0.6);
   }

   /**
    * The color light slate grey with an RGB value of #778899
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSlateGrey()
   {
      return LightSlateGray();
   }

   /**
    * The color light steel blue with an RGB value of #B0C4DE
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#B0C4DE;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightSteelBlue()
   {
      return new ColorDefinition(0.6901961, 0.76862746, 0.87058824);
   }

   /**
    * The color light yellow with an RGB value of #FFFFE0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFFE0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LightYellow()
   {
      return new ColorDefinition(1.0, 1.0, 0.8784314);
   }

   /**
    * The color lime with an RGB value of #00FF00
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00FF00;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Lime()
   {
      return new ColorDefinition(0.0, 1.0, 0.0);
   }

   /**
    * The color lime green with an RGB value of #32CD32
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#32CD32;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition LimeGreen()
   {
      return new ColorDefinition(0.19607843, 0.8039216, 0.19607843);
   }

   /**
    * The color linen with an RGB value of #FAF0E6
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FAF0E6;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Linen()
   {
      return new ColorDefinition(0.98039216, 0.9411765, 0.9019608);
   }

   /**
    * The color magenta with an RGB value of #FF00FF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Magenta()
   {
      return new ColorDefinition(1.0, 0.0, 1.0);
   }

   /**
    * The color maroon with an RGB value of #800000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#800000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Maroon()
   {
      return new ColorDefinition(0.5019608, 0.0, 0.0);
   }

   /**
    * The color medium aquamarine with an RGB value of #66CDAA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#66CDAA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumAquamarine()
   {
      return new ColorDefinition(0.4, 0.8039216, 0.6666667);
   }

   /**
    * The color medium blue with an RGB value of #0000CD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#0000CD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumBlue()
   {
      return new ColorDefinition(0.0, 0.0, 0.8039216);
   }

   /**
    * The color medium orchid with an RGB value of #BA55D3
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#BA55D3;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumOrchid()
   {
      return new ColorDefinition(0.7294118, 0.33333334, 0.827451);
   }

   /**
    * The color medium purple with an RGB value of #9370DB
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#9370DB;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumPurple()
   {
      return new ColorDefinition(0.5764706, 0.4392157, 0.85882354);
   }

   /**
    * The color medium sea green with an RGB value of #3CB371
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#3CB371;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumSeaGreen()
   {
      return new ColorDefinition(0.23529412, 0.7019608, 0.44313726);
   }

   /**
    * The color medium slate blue with an RGB value of #7B68EE
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#7B68EE;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumSlateBlue()
   {
      return new ColorDefinition(0.48235294, 0.40784314, 0.93333334);
   }

   /**
    * The color medium spring green with an RGB value of #00FA9A
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00FA9A;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumSpringGreen()
   {
      return new ColorDefinition(0.0, 0.98039216, 0.6039216);
   }

   /**
    * The color medium turquoise with an RGB value of #48D1CC
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#48D1CC;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumTurquoise()
   {
      return new ColorDefinition(0.28235295, 0.81960785, 0.8);
   }

   /**
    * The color medium violet red with an RGB value of #C71585
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#C71585;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MediumVioletRed()
   {
      return new ColorDefinition(0.78039217, 0.08235294, 0.52156866);
   }

   /**
    * The color midnight blue with an RGB value of #191970
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#191970;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MidnightBlue()
   {
      return new ColorDefinition(0.09803922, 0.09803922, 0.4392157);
   }

   /**
    * The color mint cream with an RGB value of #F5FFFA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F5FFFA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MintCream()
   {
      return new ColorDefinition(0.9607843, 1.0, 0.98039216);
   }

   /**
    * The color misty rose with an RGB value of #FFE4E1
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFE4E1;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition MistyRose()
   {
      return new ColorDefinition(1.0, 0.89411765, 0.88235295);
   }

   /**
    * The color moccasin with an RGB value of #FFE4B5
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFE4B5;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Moccasin()
   {
      return new ColorDefinition(1.0, 0.89411765, 0.70980394);
   }

   /**
    * The color navajo white with an RGB value of #FFDEAD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFDEAD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition NavajoWhite()
   {
      return new ColorDefinition(1.0, 0.87058824, 0.6784314);
   }

   /**
    * The color navy with an RGB value of #000080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#000080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Navy()
   {
      return new ColorDefinition(0.0, 0.0, 0.5019608);
   }

   /**
    * The color old lace with an RGB value of #FDF5E6
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FDF5E6;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition OldLace()
   {
      return new ColorDefinition(0.99215686, 0.9607843, 0.9019608);
   }

   /**
    * The color olive with an RGB value of #808000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#808000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Olive()
   {
      return new ColorDefinition(0.5019608, 0.5019608, 0.0);
   }

   /**
    * The color olive drab with an RGB value of #6B8E23
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#6B8E23;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition OliveDrab()
   {
      return new ColorDefinition(0.41960785, 0.5568628, 0.13725491);
   }

   /**
    * The color orange with an RGB value of #FFA500
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFA500;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Orange()
   {
      return new ColorDefinition(1.0, 0.64705884, 0.0);
   }

   /**
    * The color orange red with an RGB value of #FF4500
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF4500;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition OrangeRed()
   {
      return new ColorDefinition(1.0, 0.27058825, 0.0);
   }

   /**
    * The color orchid with an RGB value of #DA70D6
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DA70D6;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Orchid()
   {
      return new ColorDefinition(0.85490197, 0.4392157, 0.8392157);
   }

   /**
    * The color pale goldenrod with an RGB value of #EEE8AA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#EEE8AA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PaleGoldenrod()
   {
      return new ColorDefinition(0.93333334, 0.9098039, 0.6666667);
   }

   /**
    * The color pale green with an RGB value of #98FB98
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#98FB98;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PaleGreen()
   {
      return new ColorDefinition(0.59607846, 0.9843137, 0.59607846);
   }

   /**
    * The color pale turquoise with an RGB value of #AFEEEE
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#AFEEEE;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PaleTurquoise()
   {
      return new ColorDefinition(0.6862745, 0.93333334, 0.93333334);
   }

   /**
    * The color pale violet red with an RGB value of #DB7093
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DB7093;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PaleVioletRed()
   {
      return new ColorDefinition(0.85882354, 0.4392157, 0.5764706);
   }

   /**
    * The color papaya whip with an RGB value of #FFEFD5
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFEFD5;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PapayaWhip()
   {
      return new ColorDefinition(1.0, 0.9372549, 0.8352941);
   }

   /**
    * The color peach puff with an RGB value of #FFDAB9
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFDAB9;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PeachPuff()
   {
      return new ColorDefinition(1.0, 0.85490197, 0.7254902);
   }

   /**
    * The color peru with an RGB value of #CD853F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#CD853F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Peru()
   {
      return new ColorDefinition(0.8039216, 0.52156866, 0.24705882);
   }

   /**
    * The color pink with an RGB value of #FFC0CB
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFC0CB;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Pink()
   {
      return new ColorDefinition(1.0, 0.7529412, 0.79607844);
   }

   /**
    * The color plum with an RGB value of #DDA0DD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#DDA0DD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Plum()
   {
      return new ColorDefinition(0.8666667, 0.627451, 0.8666667);
   }

   /**
    * The color powder blue with an RGB value of #B0E0E6
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#B0E0E6;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition PowderBlue()
   {
      return new ColorDefinition(0.6901961, 0.8784314, 0.9019608);
   }

   /**
    * The color purple with an RGB value of #800080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#800080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Purple()
   {
      return new ColorDefinition(0.5019608, 0.0, 0.5019608);
   }

   /**
    * The color red with an RGB value of #FF0000
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF0000;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Red()
   {
      return new ColorDefinition(1.0, 0.0, 0.0);
   }

   /**
    * The color rosy brown with an RGB value of #BC8F8F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#BC8F8F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition RosyBrown()
   {
      return new ColorDefinition(0.7372549, 0.56078434, 0.56078434);
   }

   /**
    * The color royal blue with an RGB value of #4169E1
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#4169E1;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition RoyalBlue()
   {
      return new ColorDefinition(0.25490198, 0.4117647, 0.88235295);
   }

   /**
    * The color saddle brown with an RGB value of #8B4513
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#8B4513;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SaddleBrown()
   {
      return new ColorDefinition(0.54509807, 0.27058825, 0.07450981);
   }

   /**
    * The color salmon with an RGB value of #FA8072
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FA8072;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Salmon()
   {
      return new ColorDefinition(0.98039216, 0.5019608, 0.44705883);
   }

   /**
    * The color sandy brown with an RGB value of #F4A460
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F4A460;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SandyBrown()
   {
      return new ColorDefinition(0.95686275, 0.6431373, 0.3764706);
   }

   /**
    * The color sea green with an RGB value of #2E8B57
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#2E8B57;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SeaGreen()
   {
      return new ColorDefinition(0.18039216, 0.54509807, 0.34117648);
   }

   /**
    * The color sea shell with an RGB value of #FFF5EE
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFF5EE;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SeaShell()
   {
      return new ColorDefinition(1.0, 0.9607843, 0.93333334);
   }

   /**
    * The color sienna with an RGB value of #A0522D
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#A0522D;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Sienna()
   {
      return new ColorDefinition(0.627451, 0.32156864, 0.1764706);
   }

   /**
    * The color silver with an RGB value of #C0C0C0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#C0C0C0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Silver()
   {
      return new ColorDefinition(0.7529412, 0.7529412, 0.7529412);
   }

   /**
    * The color sky blue with an RGB value of #87CEEB
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#87CEEB;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SkyBlue()
   {
      return new ColorDefinition(0.5294118, 0.80784315, 0.92156863);
   }

   /**
    * The color slate blue with an RGB value of #6A5ACD
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#6A5ACD;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SlateBlue()
   {
      return new ColorDefinition(0.41568628, 0.3529412, 0.8039216);
   }

   /**
    * The color slate gray with an RGB value of #708090
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SlateGray()
   {
      return new ColorDefinition(0.4392157, 0.5019608, 0.5647059);
   }

   /**
    * The color slate grey with an RGB value of #708090
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SlateGrey()
   {
      return SlateGray();
   }

   /**
    * The color snow with an RGB value of #FFFAFA
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFAFA;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Snow()
   {
      return new ColorDefinition(1.0, 0.98039216, 0.98039216);
   }

   /**
    * The color spring green with an RGB value of #00FF7F
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#00FF7F;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SpringGreen()
   {
      return new ColorDefinition(0.0, 1.0, 0.49803922);
   }

   /**
    * The color steel blue with an RGB value of #4682B4
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#4682B4;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition SteelBlue()
   {
      return new ColorDefinition(0.27450982, 0.50980395, 0.7058824);
   }

   /**
    * The color tan with an RGB value of #D2B48C
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#D2B48C;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Tan()
   {
      return new ColorDefinition(0.8235294, 0.7058824, 0.54901963);
   }

   /**
    * The color teal with an RGB value of #008080
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#008080;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Teal()
   {
      return new ColorDefinition(0.0, 0.5019608, 0.5019608);
   }

   /**
    * The color thistle with an RGB value of #D8BFD8
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#D8BFD8;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Thistle()
   {
      return new ColorDefinition(0.84705883, 0.7490196, 0.84705883);
   }

   /**
    * The color tomato with an RGB value of #FF6347
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FF6347;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Tomato()
   {
      return new ColorDefinition(1.0, 0.3882353, 0.2784314);
   }

   /**
    * The color turquoise with an RGB value of #40E0D0
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#40E0D0;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Turquoise()
   {
      return new ColorDefinition(0.2509804, 0.8784314, 0.8156863);
   }

   /**
    * The color violet with an RGB value of #EE82EE
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#EE82EE;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Violet()
   {
      return new ColorDefinition(0.93333334, 0.50980395, 0.93333334);
   }

   /**
    * The color wheat with an RGB value of #F5DEB3
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F5DEB3;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Wheat()
   {
      return new ColorDefinition(0.9607843, 0.87058824, 0.7019608);
   }

   /**
    * The color white with an RGB value of #FFFFFF
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFFFF;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition White()
   {
      return new ColorDefinition(1.0, 1.0, 1.0);
   }

   /**
    * The color white smoke with an RGB value of #F5F5F5
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#F5F5F5;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition WhiteSmoke()
   {
      return new ColorDefinition(0.9607843, 0.9607843, 0.9607843);
   }

   /**
    * The color yellow with an RGB value of #FFFF00
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#FFFF00;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition Yellow()
   {
      return new ColorDefinition(1.0, 1.0, 0.0);
   }

   /**
    * The color yellow green with an RGB value of #9ACD32
    * 
    * <div style="border:1px solid
    * black;width:40px;height:20px;background-color:#9ACD32;float:right;margin: 0 10px 0 0"></div><br/>
    * <br/>
    */
   public static final ColorDefinition YellowGreen()
   {
      return new ColorDefinition(0.6039216, 0.8039216, 0.19607843);
   }
}
