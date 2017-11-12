import Dependencies._

name := "Smart Backpacker API"

enablePlugins(JavaAppPackaging)

lazy val commonSettings: Seq[SettingsDefinition] = Seq(
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
  coverageExcludedPackages := "com\\.github\\.gvolpe\\.smartbackpacker\\.persistence\\.static.*;.*Server*",
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

lazy val root = project.in(file("."))
  .aggregate(`smart-backpacker-api`, `smart-backpacker-airlines`)

lazy val `smart-backpacker-api` = project.in(file("api"))
  .settings(commonSettings: _*)

lazy val `smart-backpacker-airlines` = project.in(file("airlines"))
  .settings(commonSettings: _*)
  .dependsOn(`smart-backpacker-api`)
