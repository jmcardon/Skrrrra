val Http4sVersion = "0.18.0-M3"
val LogbackVersion = "1.2.3"
val tsecV = "0.0.1-M2-NAPSHO"

lazy val scalacOpts = scalacOptions := Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ypartial-unification",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val root = (project in file("."))
  .settings(
    organization := "com.example",
    name := "http4s-auth",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.3",
    scalacOpts,
    resolvers += "jmcardon at bintray" at "https://dl.bintray.com/jmcardon/tsec",
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "io.github.jmcardon"  %% "tsec-password" % tsecV,
      "io.github.jmcardon" %% "tsec-http4s" % tsecV,
      "org.typelevel" %% "cats-core" % "1.0.0-MF",
      "co.fs2" %% "fs2-core" % "0.10.0-M6"
    )
  )
