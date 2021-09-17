plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.4"
   id("us.ihmc.ihmc-cd") version "1.21"
}

ihmc {
   loadProductProperties("../group.gradle.properties")

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("us.ihmc:scs2-definition:source")
   api("us.ihmc:scs2-shared-memory:source")
   api("us.ihmc:scs2-session:source")
   api("us.ihmc:euclid:0.17.0")
   api("us.ihmc:euclid-shape:0.17.0")
   api("us.ihmc:euclid-frame:0.17.0")
   api("us.ihmc:euclid-frame-shape:0.17.0")
   api("us.ihmc:ihmc-messager:0.1.7")
   api("us.ihmc:ihmc-yovariables:0.9.11")
   api("us.ihmc:mecano-yovariables:0.8.3")
}

testDependencies {
}
