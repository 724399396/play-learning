name := """play-scala-seed"""
organization := "com.github.weili"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += filters
libraryDependencies ++= Seq(
  jdbc, evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "postgresql" % "postgresql" % "9.3-1102.jdbc41",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "net.sf.barcode4j" % "barcode4j" % "2.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3"
)