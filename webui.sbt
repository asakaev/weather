lazy val webUI = (project in file("web-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    unmanagedSourceDirectories in Compile +=
      (scalaSource in Compile).value,
    scalaJSUseMainModuleInitializer := true,
    skip in packageJSDependencies := false,
    jsDependencies ++= Seq(
      "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js"
    ),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
      "com.lihaoyi" %%% "upickle" % "0.4.4",
      "org.scala-js" %%% "scalajs-java-time" % "0.2.2"
    )
  )
