name := """play-scala-seed"""
organization := "com.github.weili"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "net.sf.barcode4j" % "barcode4j" % "2.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.github.weili.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.github.weili.binders._"
