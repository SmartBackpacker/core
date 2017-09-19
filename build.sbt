import Dependencies._

lazy val smartBackpaker = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.gvolpe",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Smart Backpacker API",
    libraryDependencies ++= Seq(
      http4sClient,
      http4sDsl,
      http4sCirce,
      typesafeConfig,
      logback,
      scalaTest % Test
    )
  )
