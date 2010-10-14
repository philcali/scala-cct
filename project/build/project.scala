import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  // scalate for xml templates
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.2"
  val scalaterepo = "Scalate Repo" at "http://repo.fusesource.com/nexus/content/repositories/public/org/fusesource/scalate/"

  // Grizzled Repo
  val grizzled = "org.clapper" %% "grizzled-scala" % "1.0"
 
  // Scala check
  val scalacheck = "org.scalatest" % "scalatest" % "1.2"

  // Logging framework
  val sl4j = "org.slf4j" % "slf4j-api" % "1.6.1"
  val simple = "org.slf4j" % "slf4j-simple" % "1.6.1"
 
  // Unfiltered Library
  val ufj = "net.databinder" %% "unfiltered-jetty" % "0.2.0"
  val uff = "net.databinder" %% "unfiltered-filter" % "0.2.0"
  val ufs = "net.databinder" %% "unfiltered-scalate" % "0.2.0"
  val ufu = "net.databinder" %% "unfiltered-uploads" % "0.2.0"
}
