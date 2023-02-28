plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.7"
   id("us.ihmc.ihmc-cd") version "1.23"
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
}
