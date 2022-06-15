plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.6"
   id("us.ihmc.ihmc-cd") version "1.23"
}

ihmc {
   loadProductProperties("../group.gradle.properties")

   configureDependencyResolution()
   repository("https://oss.sonatype.org/content/repositories/snapshots")
   configurePublications()
}

val javaCPPVersion = "1.5.8-SNAPSHOT"

mainDependencies {
   api("us.ihmc:scs2-simulation:source")
   api("us.ihmc:scs2-definition:source")
   api("us.ihmc:scs2-shared-memory:source")
   api("us.ihmc:scs2-session:source")
   api("us.ihmc:euclid-frame-shape:0.17.2")
   api("us.ihmc:ihmc-messager:0.1.7")
   api("us.ihmc:ihmc-yovariables:0.9.13")
   api("us.ihmc:mecano-yovariables:0.11.1")

   api("us.ihmc:ihmc-javafx-toolkit:0.21.1") {
      exclude(group="us.ihmc", module="jassimp")
      exclude(group="us.ihmc", module="euclid")
      exclude(group="us.ihmc", module="euclid-shape")
      exclude(group="us.ihmc", module="euclid-frame")
   }

   apiBytedecoNatives("javacpp")
   apiBytedecoNatives("bullet", "3.24-")
   
   val lwjglVersion = "3.3.1"
   api("org.lwjgl:lwjgl-assimp:$lwjglVersion")
   api("org.lwjgl:lwjgl-assimp:$lwjglVersion:natives-linux")
   api("org.lwjgl:lwjgl-assimp:$lwjglVersion:natives-windows")
   api("org.lwjgl:lwjgl-assimp:$lwjglVersion:natives-windows-x86")
   api("org.lwjgl:lwjgl-assimp:$lwjglVersion:natives-macos")
}

testDependencies {
   api("us.ihmc:scs2-session-visualizer-jfx:source")
}

fun us.ihmc.build.IHMCDependenciesExtension.apiBytedecoNatives(name: String, versionPrefix: String = "")
{
   apiBytedecoSelective("org.bytedeco:$name:$versionPrefix$javaCPPVersion")
   apiBytedecoSelective("org.bytedeco:$name:$versionPrefix$javaCPPVersion:linux-x86_64")
   if (name != "spinnaker")
      apiBytedecoSelective("org.bytedeco:$name:$versionPrefix$javaCPPVersion:linux-arm64")
   apiBytedecoSelective("org.bytedeco:$name:$versionPrefix$javaCPPVersion:windows-x86_64")
   if (name != "spinnaker")
      apiBytedecoSelective("org.bytedeco:$name:$versionPrefix$javaCPPVersion:macosx-x86_64")
}

fun us.ihmc.build.IHMCDependenciesExtension.apiBytedecoSelective(dependencyNotation: String)
{
   api(dependencyNotation) {
      exclude(group = "org.bytedeco")
   }
}
