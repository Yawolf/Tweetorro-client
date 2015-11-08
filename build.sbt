
lazy val commonSettings = Seq(
  organization := "org.potorrosl",
  version := "0.1.0",
  // set the Scala version used for the project
  scalaVersion := "2.11.7"
)

name:="Client"

scalaVersion:="2.11.7"

lazy val root = (project in file(".")).
    settings(commonSettings: _*).
  settings(
    // set the name of the project
    name := "Client",
    scalaSource in Compile := baseDirectory.value / "src",

    mainClass in (Compile, run) := Some("client.Client")

  )
