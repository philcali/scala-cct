// Scala Course Converter is a command-line utility that gives the end user
// the ability to convert one e-learning management system course archive
// to another e-learning management system course archive.
// 
// 
// Copyright (C) 2010 Philip Cali 
// 
// 
// Scala Course Converter is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
import sbt._

// TODO: Maybe look at conscript harnessing this
class Project(info: ProjectInfo) extends DefaultProject(info) 
                                 with assembly.AssemblyBuilder { 
  // scalate for xml templates
  val scalaterepo = "Scalate Repo" at "http://repo.fusesource.com/nexus/content/repositories/public/"
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.2"

  // Grizzled Repo
  val grizzled = "org.clapper" %% "grizzled-scala" % "1.0.3"
 
  // Class util
  val classutil = "org.clapper" %% "classutil" % "0.3.2"

  // Scala test 
  val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

  // Logging framework
  /*
  val sl4j = "org.slf4j" % "slf4j-api" % "1.6.1"
  val simple = "org.slf4j" % "slf4j-simple" % "1.6.1"
  */ 

  // Unfiltered Library
  val ufj = "net.databinder" %% "unfiltered-jetty" % "0.3.1"
  val uff = "net.databinder" %% "unfiltered-filter" % "0.3.1"
  // Unfortunately, the scalate requires 2.8.0
  val ufs = "net.databinder" % "unfiltered-scalate_2.8.0" % "0.3.1"
  val ufu = "net.databinder" %% "unfiltered-uploads" % "0.3.1"

  override def mainClass = Some("com.philipcali.cct.Converter")

  /*
  override def proguardInJars = super.proguardInJars +++ scalaLibraryPath
  override def proguardOptions = List(
    proguardKeepMain(mainClass.get),
    proguardKeepLimitedSerializability,
    proguardKeepAllScala,
    """-keep public class * {
          public protected *;
       }

       -keepclassmembernames class * {
          java.lang.Class class$(java.lang.String);
          java.lang.Class class$(java.lang.String, boolean);
       }

       -keepclasseswithmembernames class * {
          native <methods>;
       }""",
    "-keep class * { public ** tag(); }",
    "-keep class * { <init>(***); }"
  )
  */

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

    /*
    val newFile = new java.io.File(newPath)
    newFile.delete
    */
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
