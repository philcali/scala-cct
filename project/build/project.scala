import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  // scalate for xml templates
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.2"
  val scalaterepo = "Scalate Repo" at "http://repo.fusesource.com/nexus/content/repositories/public/org/fusesource/scalate/"

  // Grizzled Repo
  val grizzled = "org.clapper" %% "grizzled-scala" % "1.0"
 
  // Scala check
  val scalacheck = "org.scalatest" % "scalatest" % "1.2"
 
  // Unfiltered for dedicated web server
  val unfiltered = "net.databinder" %% "unfiltered-server" % "0.1.4"
}
