import Dependencies._

enablePlugins(JavaAppPackaging)

lazy val smartBackpaker = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.gvolpe",
      scalaVersion := "2.12.3",
      version      := "0.1.0",
      scalacOptions := Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-Ypartial-unification"
      )
    )),
    name := "Smart Backpacker API",
    coverageExcludedPackages := ".*TripAdvisorAirlinesParser*;.*Server*",
    libraryDependencies ++= Seq(
      http4sServer,
      http4sClient,
      http4sDsl,
      http4sCirce,
      circe,
      circeGeneric,
      scalaScraper,
      typesafeConfig,
      logback,
      scalaTest,
      scalaCheck
    )
  )
