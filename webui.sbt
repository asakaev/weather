val circeVersion = "0.8.0"

lazy val webUI = (project in file("web-ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    unmanagedSourceDirectories in Compile +=
      (scalaSource in Compile).value,
    scalaJSUseMainModuleInitializer := true,
    skip in packageJSDependencies := false,
    jsDependencies ++= Seq(
      "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js",
      "org.webjars.npm" % "js-joda" % "1.5.1" / "dist/js-joda.js" minified "dist/js-joda.min.js"
    ),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "com.zoepepper" %%% "scalajs-jsjoda" % "1.1.1",
      "org.singlespaced" %%% "scalajs-d3" % "0.3.4"
    )
  )
