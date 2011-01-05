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

  lazy val makeExec = task {
    val command = "zip -r %s/program.zip %s %s" format (outputPath, jarPath, managedDependencyPath)
    try {
      log.info("Attempting to build program archive")
      withScalaJar { 
        Runtime.getRuntime().exec(command)
      }
      log.info("Creating shell script")
      createScript()
      log.info("Making shell script executable")
      Runtime.getRuntime().exec("chmod +x %s" format(outputPath / "cct"))
      log.info("... Done")
    } catch {
      case e: Exception => log.error("Could not build program archive. This action relies on 'zip' and 'chmod' executables")
    }
    None
  } dependsOn(`package`) describedAs("Packages executable")

  def withScalaJar(op: => Unit) {
    val newPath = "%s/compile/scala-library.jar" format(managedDependencyPath)
    val reader = new java.io.FileInputStream(
                 mainDependencies.scalaJars.getPaths.find(_.contains("scala-library")).get)
    val writer = new java.io.FileOutputStream(newPath)

    // Copy the scala library jar
    def copy(in: java.io.InputStream, out: java.io.OutputStream): Unit = {
      val b = new Array[Byte](1024)
      in.read(b) match {
        case n if n > -1 => out.write(b, 0, n); copy(in, out)
        case _ => in.close(); out.close()
      }
    }
    copy(reader, writer)  

    // Perform the operation
    op

    val newFile = new java.io.File(newPath)
    newFile.delete
  }

  def createScript() {
    def sep(s: Path) = s.toString.split("/")
    def prepender(xs: Path) = "cct_program/" + sep(xs).drop(1).mkString("/")

    val jarSplit = prepender(jarPath)
    val manSplit = prepender(managedDependencyPath) 

    val writer = new java.io.FileWriter((outputPath / "cct").toString)
    writer.write("#! /bin/sh\n")
    writer.write("unzip -qq program.zip -d cct_program\n")
    writer.write("java -classpath %s/compile/*:%s %s \"$@\"\n" format(manSplit, jarSplit, mainClass.get))
    writer.write("rm -rf cct_program") 
    writer.close()
  }
}
