import sbt.Keys._

name := "bookstore"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  javaJpa,
  "com.notnoop.apns" % "apns" % "1.0.0.Beta6" ,
  "mysql" % "mysql-connector-java" % "5.1.36",
  "commons-codec" % "commons-codec" % "1.8" force(),
  "com.google.code.gson" % "gson" % "2.6.2",
  "com.edulify" %% "geolocation" % "2.0.0",
  filters
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "Sonatype OSS Snasphots" at "http://oss.sonatype.org/content/repositories/snapshots",
  Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
)
routesGenerator := InjectedRoutesGenerator

lazy val bookstore = (project in file(".")).enablePlugins(PlayJava, PlayEbean)