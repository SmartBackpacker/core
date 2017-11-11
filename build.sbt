import Dependencies._

enablePlugins(JavaAppPackaging)

lazy val smartBackpaker = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.gvolpe",
      scalaVersion := "2.12.3",
      version      := "0.2.0",
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
    coverageExcludedPackages := "com\\.github\\.gvolpe\\.smartbackpacker\\.persistence\\.static.*;.*Server*",
    libraryDependencies ++= Seq(
      http4sServer,
      http4sClient,
      http4sDsl,
      http4sCirce,
      circe,
      circeGeneric,
      doobieCore,
      doobiePostgres,
      scalaScraper,
      typesafeConfig,
      logback,
      scalaTest,
      scalaCheck
    )
  )
