lazy val weather = (project in file("."))
  .settings(
    name := "Weather",
    scalaVersion := "2.12.2",
    version := "0.0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.7",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.6",
      "org.scalactic" %% "scalactic" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )
