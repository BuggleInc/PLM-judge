import AssemblyKeys._  // put this at the top of the file

assemblySettings

organization := "inc.buggle"

name := "PLM-judge"

version := "2.0.0-rc3"

autoScalaLibrary := false

crossPaths := false

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.10",
  "com.novocode" % "junit-interface" % "0.10-M1" % "test"
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mainClass in (Compile,run) := Some("main.java.Main")
mainClass in assembly := Some("main.java.Main")
