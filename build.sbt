import AssemblyKeys._  // put this at the top of the file

assemblySettings

organization := "inc.buggle"

name := "PLM-judge"

version := "1.0-SNAPSHOT"

autoScalaLibrary := false

crossPaths := false

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

scalaVersion := "2.11.4"

val kamonVersion = "0.5.2"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.10",
  "com.novocode" % "junit-interface" % "0.10-M1" % "test"
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
