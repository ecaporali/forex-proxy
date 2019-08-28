import sbt._

object Dependencies {

  object Versions {
    val cats                = "1.6.1"
    val catsEffect          = "1.4.0"
    val fs2                 = "1.0.5"
    val http4s              = "0.20.0"
    val circe               = "0.11.1"
    val pureConfig          = "0.11.1"
    val enumeratum          = "1.5.13"
    val log4cats            = "1.0.0-RC1"
    val scalaCache          = "0.28.0"

    val kindProjector       = "0.9.10"
    val logback             = "1.2.3"
    val scalaCheck          = "1.14.0"
    val scalaTest           = "3.0.8"
    val catsScalaCheck      = "0.1.1"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s
    def pureConfig(artifact: String): ModuleID = "com.github.pureconfig" %% artifact % Versions.pureConfig
    def log4cats(artifact: String): ModuleID   = "io.chrisdavenport"     %% artifact  % Versions.log4cats
    def scalaCache(artifact: String): ModuleID = "com.github.cb372"      %% artifact  % Versions.scalaCache

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2
    lazy val enumeratum          = "com.beachape"          %% "enumeratum"                 % Versions.enumeratum

    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sClient        = http4s("http4s-blaze-client")
    lazy val http4sCirce         = http4s("http4s-circe")
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")
    lazy val circeJava8          = circe("circe-java8")
    lazy val pureConfigCore      = pureConfig("pureconfig")
    lazy val pureConfigHttp4s    = pureConfig("pureconfig-http4s")
    lazy val log4catsCore        = log4cats("log4cats-core")
    lazy val log4catsSlf4j       = log4cats("log4cats-slf4j")
    lazy val log4catsNoop        = log4cats("log4cats-noop")
    lazy val scalaCacheCaffeine  = scalaCache("scalacache-caffeine")
    lazy val scalaCacheEffect    = scalaCache("scalacache-cats-effect")

    // Compiler plugins
    lazy val kindProjector       = "org.spire-math"        %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val logback             = "ch.qos.logback"        %  "logback-classic"            % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport"     %% "cats-scalacheck"            % Versions.catsScalaCheck
  }

}
