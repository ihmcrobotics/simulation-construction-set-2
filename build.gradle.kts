plugins {
   id("us.ihmc.ihmc-build") version "0.19.7"
   id("us.ihmc.ihmc-ci") version "5.3"
   id("us.ihmc.ihmc-cd") version "1.14"
}

ihmc {
   group = "us.ihmc"
   version = "alpha-20191122"
   vcsUrl = "https://github.com/ihmcrobotics/simulation-construction-set-2"
   openSource = true

   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   // TODO This should be removed, it is only a workaround for the task deployApplications.
   api(ihmc.sourceSetProject("session-visualizer"))
}

definitionDependencies {
   api("us.ihmc:euclid:0.12.1")
   api("us.ihmc:ihmc-commons:0.26.3")
   api("us.ihmc:ihmc-yovariables:0.3.11")
   api("us.ihmc:mecano:0.0.20")
}

sharedMemoryDependencies {
   api("us.ihmc:ihmc-yovariables:0.3.11")
}

sessionDependencies {
   api(ihmc.sourceSetProject("definition"))
   api(ihmc.sourceSetProject("shared-memory"))

   api("us.ihmc:ihmc-messager:0.1.2")
}

simulationDependencies {
   api(ihmc.sourceSetProject("definition"))
   api(ihmc.sourceSetProject("shared-memory"))
   api(ihmc.sourceSetProject("session"))
   api("us.ihmc:euclid-shape:0.12.1")
   api("us.ihmc:ihmc-messager:0.1.3")
   api("us.ihmc:mecano-yovariables:0.0.20")
}

sessionVisualizerDependencies {
   api(ihmc.sourceSetProject("simulation"))
   api(ihmc.sourceSetProject("session"))

   compile("us.ihmc:ihmc-javafx-toolkit:0.12.11") {
      exclude(group="us.ihmc", module="jassimp")
   }
   api("us.ihmc:ihmc-graphics-description:0.12.13")
   api("us.ihmc:IHMCVideoCodecs:2.1.3")
   api("us.ihmc:ihmc-robot-data-logger:0.12.2")
   api("us.ihmc:svgloader:0.0")
   api("us.ihmc:ihmc-javafx-extensions:0.0")

   api("com.google.guava:guava:18.0")
   api("org.reflections:reflections:0.9.10")

   // JavaFX extensions
   api("org.controlsfx:controlsfx:8.40.15")
   api("de.jensd:fontawesomefx:8.9")
   api("com.jfoenix:jfoenix:8.0.9")
   api("org.apache.commons:commons-text:1.4")

   api(fileTree(mapOf("dir" to "src/session-visualizer/libs/JavaFXModelImporters", "include" to "*.jar")))
}

examplesDependencies {
   api(ihmc.sourceSetProject("session-visualizer"))
}

testDependencies {
   api(ihmc.sourceSetProject("definition"))
   api(ihmc.sourceSetProject("shared-memory"))
   api(ihmc.sourceSetProject("simulation"))
   api(ihmc.sourceSetProject("session-visualizer"))
   api(ihmc.sourceSetProject("examples"))

   api("org.junit.platform:junit-platform-commons:1.5.2")
}

ihmc.jarWithLibFolder()
tasks.getByPath("installDist").dependsOn("compositeJar")

app.entrypoint("SessionVisualizer", "us.ihmc.scs2.sessionVisualizer.SessionVisualizer")

tasks.create("deployApplications") {
   dependsOn("installDist")

   doLast {
      val appFolder = File(System.getProperty("user.home"), "ihmc_apps/SCS2-" + version)
      appFolder.delete()
      appFolder.mkdirs()
      copy {
         from("build/install/simulation-construction-set-2")
         into(appFolder)
      }
      println("-------------------------------------------------------------------------")
      println("------- Deployed files to: " + appFolder.path + " -------")
      println("-------------------------------------------------------------------------")
   }
}