import sbt._

object Dependencies {
  lazy val Http4sVersion  = "0.18.0-M1"
  lazy val CirceVersion   = "0.9.0-M1"

  lazy val http4sServer   = "org.http4s"        %% "http4s-blaze-server"  % Http4sVersion
  lazy val http4sClient   = "org.http4s"        %% "http4s-blaze-client"  % Http4sVersion
  lazy val http4sDsl      = "org.http4s"        %% "http4s-dsl"           % Http4sVersion
  lazy val http4sCirce    = "org.http4s"        %% "http4s-circe"         % Http4sVersion
  lazy val circe          = "io.circe"          %% "circe-core"           % CirceVersion
  lazy val circeGeneric   = "io.circe"          %% "circe-generic"        % CirceVersion

  lazy val scalaScraper   = "net.ruippeixotog"  %% "scala-scraper"        % "2.0.0"

  lazy val typesafeConfig = "com.typesafe"      % "config"                % "1.3.1"
  lazy val logback        = "ch.qos.logback"    %  "logback-classic"      % "1.2.1"

  lazy val scalaTest      = "org.scalatest"     %% "scalatest"            % "3.0.3"
}
