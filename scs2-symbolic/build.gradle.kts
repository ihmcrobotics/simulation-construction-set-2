plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "8.3"
   id("us.ihmc.ihmc-cd") version "1.26"
}

ihmc {
   loadProductProperties("../group.gradle.properties")

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("us.ihmc:scs2-definition:source")
   api("us.ihmc:scs2-shared-memory:source")

   api("us.ihmc:euclid:0.20.0")
   api("us.ihmc:euclid-frame:0.20.0")
   api("us.ihmc:ihmc-yovariables:0.10.0")
}

testDependencies {
}