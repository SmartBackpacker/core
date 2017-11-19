import Dependencies._
import sbt.ModuleID

name := "Smart Backpacker API"

lazy val commonSettings: Seq[SettingsDefinition] = Seq(
  inThisBuild(List(
    organization := "com.github.gvolpe",
    scalaVersion := "2.12.3",
    version      := "0.2.1",
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
  // TODO: The following objects / classes should be exluded but currently it's not possible: https://github.com/scoverage/sbt-scoverage/issues/245
  //;.*VisaRestrictionsIndexParser.*";AirlineDao*;ExchangeRateService*;AirlineService*
  coverageExcludedPackages := "com\\.github\\.gvolpe\\.smartbackpacker\\.persistence\\.static.*;.*Server*;.*AirlinesJob*;.*IOApp*",
  libraryDependencies ++= Seq(
    http4sServer,
    http4sClient,
    http4sDsl,
    http4sCirce,
    circe,
    circeGeneric,
    h2,
    doobieCore,
    doobieH2,
    doobiePostgres,
    doobieTest,
    scalaScraper,
    typesafeConfig,
    logback,
    scalaTest,
    scalaCheck
  ),
  pomExtra :=
    <scm>
      <url>git@github.com:gvolpe/smart-backpacker-api.git</url>
      <connection>scm:git:git@github.com:gvolpe/smart-backpacker-api.git</connection>
    </scm>
      <developers>
        <developer>
          <id>gvolpe</id>
          <name>Gabriel Volpe</name>
          <url>http://github.com/gvolpe</url>
        </developer>
      </developers>
)

val AirlinesDependencies: Seq[ModuleID] = Seq(
  fs2Core, fs2IO
)

lazy val root = project.in(file("."))
  .aggregate(api, airlines)

lazy val api = project.in(file("api"))
  .settings(commonSettings: _*)
  .enablePlugins(JavaAppPackaging)

lazy val airlines = project.in(file("airlines"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= AirlinesDependencies)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(api)
