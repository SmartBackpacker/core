import Dependencies._
import sbt.ModuleID

name := "Smart Backpacker Backend"

lazy val commonSettings: Seq[SettingsDefinition] = Seq(
  inThisBuild(List(
    organization := "com.github.gvolpe",
    scalaVersion := "2.12.3",
    version      := "1.0.0",
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
  parallelExecution in Test := true,
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += "jmcardon at bintray" at "https://dl.bintray.com/jmcardon/tsec",
  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.5" cross CrossVersion.binary),
  // TODO: The following objects / classes should be excluded but currently it's not possible: https://github.com/scoverage/sbt-scoverage/issues/245
  //;.*ExchangeRateService*, VisaRequirementsParser, VisaRestrictionsIndexParser
  coverageExcludedPackages := ".*Server*;.*Bindings*;.*IOApp*;.*IOAssertion*;.*AirlinesJob*;.*ScraperJob*;.*ApiTokenGenerator*;.*TokenGeneration*;.*VisaRequirementsInsertData*;.*JwtTokenAuthMiddleware*;.*Module*;.*ScraperModule*;.*AirlinesModule*",
  libraryDependencies ++= Seq(
    http4sServer,
    http4sClient,
    http4sDsl,
    http4sCirce,
    tsecJwtMac,
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
  .aggregate(api, airlines, scraper)

lazy val common = project.in(file("common"))
  .settings(commonSettings: _*)

lazy val api = project.in(file("api"))
  .settings(commonSettings: _*)
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)

lazy val airlines = project.in(file("airlines"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= AirlinesDependencies)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(api)

lazy val scraper = project.in(file("scraper"))
  .settings(commonSettings: _*)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(api)
