lazy val weather = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(
    name := "Weather",
    scalaVersion := "2.12.2",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.7",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.6", // TODO: remove
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "ch.megard" %% "akka-http-cors" % "0.2.1",
      "org.json4s" %% "json4s-native" % "3.5.2"
    ),
    dockerAutoPackageJavaApplication("openjdk:8-jre-alpine", Seq(8080))
  )
