import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  // scalate for xml templates
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.2"
  val scalaterepo = "Scalate Repo" at "http://repo.fusesource.com/nexus/content/repositories/public/org/fusesource/scalate/"

  // Grizzled Repo
  val grizzled = "org.clapper" %% "grizzled-scala" % "1.0.1"
 
  // Class util
  val classutil = "org.clapper" %% "classutil" % "0.3.2"

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

  override def mainClass = Some("com.philipcali.cct.Converter")

  override def packageAction = task { 
    packageTask(packagePaths, jarPath, packageOptions)
    val command = "zip -r %s/program.zip %s %s" format (outputPath, jarPath, managedDependencyPath)
    Runtime.getRuntime().exec(command)
    createScript()
    Runtime.getRuntime().exec("chmod +x %s" format(outputPath / "cct"))
    None
  } dependsOn(compile) describedAs("Packages jar, plus creates a cct script for server machines")

  def createScript() {
    val jarSplit = jarPath.toString.split("/")
    val manSplit = managedDependencyPath.toString.split("/")

    def prepender(xs: Array[String]) = "cct_program/" + xs.drop(1).mkString("/")

    val writer = new java.io.FileWriter((outputPath / "cct").toString)
    writer.write("#! /bin/sh\n")
    writer.write("unzip -qq program.zip -d cct_program\n")
    writer.write("scala -classpath %s:%s/compile/* %s \"$@\"\n" format(prepender(jarSplit), prepender(manSplit), mainClass.get))
    writer.write("rm -rf cct_program") 
    writer.close()
  }
}
