plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "8.3"
}

ihmc {
   loadProductProperties("../group.gradle.properties")

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("us.ihmc:scs2-simulation-construction-set:source")
   api("us.ihmc:scs2-session-visualizer-jfx:source")
   api("us.ihmc:scs2-bullet-simulation:source")
   api("us.ihmc:scs2-bullet-simulation-debug:source")

   api("org.fxyz3d:fxyz3d:0.6.0")
}
