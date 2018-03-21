import sbt._

object Dependencies {

  object Versions {
    val CatsEffect  = "0.10"
    val Monix       = "3.0.0-RC1"
    val Fs2         = "0.10.3"
    val Http4s      = "0.18.3"
    val Tsec        = "0.0.1-M9"
    val Circe       = "0.9.2"
    val Doobie      = "0.5.1"
    val H2          = "1.4.196"
    val Flyway      = "5.0.5"
    val Scraper     = "2.0.0"
    val ScalaTest   = "3.0.3"
    val ScalaCheck  = "1.13.4"
    val Logback     = "1.2.1"
    val TypesafeCfg = "1.3.1"
    val Metrics     = "4.0.0"
  }

  object Libraries {
    def doobie(artifact: String): ModuleID  = "org.tpolecat"  %% artifact % Versions.Doobie
    def circe(artifact: String): ModuleID   = "io.circe"      %% artifact % Versions.Circe
    def fs2(artifact: String): ModuleID     = "co.fs2"        %% artifact % Versions.Fs2
    def http4s(artifact: String): ModuleID  = "org.http4s"    %% artifact % Versions.Http4s

    lazy val catsEffect     = "org.typelevel"       %% "cats-effect"      % Versions.CatsEffect
    lazy val monix          = "io.monix"            %% "monix"            % Versions.Monix

    lazy val fs2Core        = fs2("fs2-core")
    lazy val fs2IO          = fs2("fs2-io")

    lazy val http4sServer   = http4s("http4s-blaze-server")
    lazy val http4sClient   = http4s("http4s-blaze-client")
    lazy val http4sDsl      = http4s("http4s-dsl")
    lazy val http4sCirce    = http4s("http4s-circe")

    lazy val tsecJwtMac     = "io.github.jmcardon"  %% "tsec-jwt-mac"     % Versions.Tsec

    lazy val circeCore      = circe("circe-core")
    lazy val circeGeneric   = circe("circe-generic")
    lazy val circeGenericX  = circe("circe-generic-extras")

    lazy val flyway         = "org.flywaydb"        %  "flyway-core"      % Versions.Flyway
    lazy val h2             = "com.h2database"      %  "h2"               % Versions.H2

    lazy val doobieCore     = doobie("doobie-core")
    lazy val doobiePostgres = doobie("doobie-postgres")
    lazy val doobieH2       = doobie("doobie-h2")
    lazy val doobieTest     = doobie("doobie-scalatest")

    lazy val scalaScraper   = "net.ruippeixotog"    %% "scala-scraper"    % Versions.Scraper

    lazy val typesafeConfig = "com.typesafe"        %  "config"           % Versions.TypesafeCfg
    lazy val logback        = "ch.qos.logback"      %  "logback-classic"  % Versions.Logback

    lazy val scalaTest      = "org.scalatest"       %% "scalatest"        % Versions.ScalaTest   % Test
    lazy val scalaCheck     = "org.scalacheck"      %% "scalacheck"       % Versions.ScalaCheck  % Test

    def metrics(artifact: String): ModuleID = "io.dropwizard.metrics" % artifact % Versions.Metrics

    lazy val metricsCore      = metrics("metrics-core")
    lazy val metricsGraphite  = metrics("metrics-graphite")
  }

}
