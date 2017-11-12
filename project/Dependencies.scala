import sbt._

object Dependencies {
  lazy val Http4sVersion  = "0.18.0-M5"
  lazy val CirceVersion   = "0.9.0-M2"
  lazy val DoobieVersion  = "0.5.0-M9"
  lazy val H2Version      = "1.4.196"

  lazy val http4sServer   = "org.http4s"        %% "http4s-blaze-server"          % Http4sVersion
  lazy val http4sClient   = "org.http4s"        %% "http4s-blaze-client"          % Http4sVersion
  lazy val http4sDsl      = "org.http4s"        %% "http4s-dsl"                   % Http4sVersion
  lazy val http4sCirce    = "org.http4s"        %% "http4s-circe"                 % Http4sVersion
  lazy val circe          = "io.circe"          %% "circe-core"                   % CirceVersion
  lazy val circeGeneric   = "io.circe"          %% "circe-generic"                % CirceVersion

  lazy val h2             = "com.h2database"    %  "h2"                           % H2Version
  lazy val doobieCore     = "org.tpolecat"      %% "doobie-core"                  % DoobieVersion
  lazy val doobiePostgres = "org.tpolecat"      %% "doobie-postgres"              % DoobieVersion
  lazy val doobieH2       = "org.tpolecat"      %% "doobie-h2"                    % DoobieVersion
  lazy val doobieTest     = "org.tpolecat"      %% "doobie-scalatest"             % DoobieVersion

  lazy val scalaScraper   = "net.ruippeixotog"  %% "scala-scraper"                % "2.0.0"

  lazy val typesafeConfig = "com.typesafe"      %  "config"                       % "1.3.1"
  lazy val logback        = "ch.qos.logback"    %  "logback-classic"              % "1.2.1"

  lazy val scalaTest      = "org.scalatest"     %% "scalatest"                    % "3.0.3"   % Test
  lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"                   % "1.13.4"  % Test
}
