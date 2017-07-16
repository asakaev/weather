lazy val webUI = (project in file("web-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    scalaJSUseMainModuleInitializer := true
  )
