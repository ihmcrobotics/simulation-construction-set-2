plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.4"
   id("us.ihmc.ihmc-cd") version "1.20"
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
//   api(ihmc.sourceSetProject("session-visualizer-jfx"))
}

definitionDependencies {
   api("us.ihmc:euclid:0.16.2")
   api("us.ihmc:euclid-shape:0.16.2")
   api("us.ihmc:ihmc-commons:0.30.4")
   api("us.ihmc:ihmc-yovariables:0.9.9")
   api("us.ihmc:mecano:0.8.2")
}

sharedMemoryDependencies {
   api("us.ihmc:ihmc-yovariables:0.9.9")
}

sessionDependencies {
   api(ihmc.sourceSetProject("definition"))
   api(ihmc.sourceSetProject("shared-memory"))

   api("us.ihmc:ihmc-messager:0.1.7")
}

simulationDependencies {
   api(ihmc.sourceSetProject("definition"))
   api(ihmc.sourceSetProject("shared-memory"))
   api(ihmc.sourceSetProject("session"))
   api("us.ihmc:euclid-frame-shape:0.16.2")
   api("us.ihmc:ihmc-messager:0.1.7")
   api("us.ihmc:mecano-yovariables:0.8.2")
}

//sessionVisualizerDependencies {
//   api(ihmc.sourceSetProject("simulation"))
//   api(ihmc.sourceSetProject("definition"))
//
//   var javaFXVersion = "15.0.1"
//   api(ihmc.javaFXModule("base", javaFXVersion)) // This is for using the property data structure. Not sure if that's the best thing to do.
//}
//
//sessionVisualizerJfxDependencies {
//   api(ihmc.sourceSetProject("simulation"))
//   api(ihmc.sourceSetProject("session"))
//   api(ihmc.sourceSetProject("session-visualizer"))
//
//   var javaFXVersion = "15.0.1"
//   api(ihmc.javaFXModule("base", javaFXVersion))
//   api(ihmc.javaFXModule("controls", javaFXVersion))
//   api(ihmc.javaFXModule("graphics", javaFXVersion))
//   api(ihmc.javaFXModule("fxml", javaFXVersion))
//   api(ihmc.javaFXModule("swing", javaFXVersion))
//
//   compile("us.ihmc:ihmc-javafx-toolkit:0.19.3") {
//      exclude(group="us.ihmc", module="jassimp")
//   }
//   api("us.ihmc:ihmc-graphics-description:0.19.3")
//   api("us.ihmc:ihmc-video-codecs:2.1.6")
//   api("us.ihmc:ihmc-robot-data-logger:0.20.9")
//   api("us.ihmc:svgloader:0.0")
//   api("us.ihmc:ihmc-javafx-extensions:15-0.0.3")
//
//   api("org.reflections:reflections:0.9.11")
//
//   // JavaFX extensions
//   api("org.controlsfx:controlsfx:11.0.1")
//   api("de.jensd:fontawesomefx-commons:9.1.2")
//   api("de.jensd:fontawesomefx-octicons:4.3.0-9.1.2")
//   api("de.jensd:fontawesomefx-materialicons:2.2.0-9.1.2")
//   api("de.jensd:fontawesomefx-materialdesignfont:2.0.26-9.1.2")
//   api("de.jensd:fontawesomefx-fontawesome:4.7.0-9.1.2")
//   api("com.jfoenix:jfoenix:9.0.10")
//   api("org.apache.commons:commons-text:1.9")
//
//   api(fileTree(mapOf("dir" to "src/session-visualizer-jfx/libs/JavaFXModelImporters", "include" to "*.jar")))
//}
//
//examplesDependencies {
//   api(ihmc.sourceSetProject("session-visualizer-jfx"))
//}
//
//testDependencies {
//   api(ihmc.sourceSetProject("definition"))
//   api(ihmc.sourceSetProject("shared-memory"))
//   api(ihmc.sourceSetProject("simulation"))
//   api(ihmc.sourceSetProject("session-visualizer-jfx"))
//   api(ihmc.sourceSetProject("examples"))
//}

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