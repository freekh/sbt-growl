name := "sbt-growl"

scalaVersion := "2.9.2"

organization := "com.groosker"

version := "1.0.0"

sbtPlugin := true

resolvers += Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.5"
