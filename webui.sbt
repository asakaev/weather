lazy val webUI = (project in file("web-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    scalaJSUseMainModuleInitializer := true
  )
