import AssemblyKeys._

seq(assemblySettings: _*)

name := "scala-cct"

organization := "com.github.philcali"

version := "0.1.0"

scalacOptions += "-deprecation"

scalaVersion := "2.9.1"

mainClass in (Compile, run) := Some("com.github.philcali.cct.Converter")

resolvers += "Scalate Repo" at 
             "http://repo.fusesource.com/nexus/content/repositories/public/"

parallelExecution in Test := false

libraryDependencies ++= Seq( 
  "net.databinder" %% "unfiltered-jetty" % "0.5.1",
  "net.databinder" %% "unfiltered-uploads" % "0.5.1",
  "org.scalatest" %% "scalatest" % "1.6.1",
  "org.clapper" %% "classutil" % "0.4.3"
)
