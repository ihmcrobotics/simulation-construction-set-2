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
   api("us.ihmc:scs2-definition:source")

   var javaFXVersion = "17.0.9"
   api(ihmc.javaFXModule("base", javaFXVersion)) // This is for using the property data structure. Not sure if that's the best thing to do.
}

testDependencies {
}
