package com.philipcali.cct

import unfiltered.jetty.Http
import unfiltered.filter._
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate.Scalate

//import org.fusesource.scalate._

import finder.MetaFinder
import Utils._
import system._

/**
 * The Converter object is the main entry point for the commandline
 * base course converter app.
 *
 * Example usage:

{{{
    cct [--web] [--port=80] [--knowledge=blackboard[:package]] [-r] [--input=path] 
        [--transformer=moodle[:package]] [--output=directory]
    
    Options:
    -h, --help         | prints out this help
    -w, --web          | starts a web interface
    -p, --port         | runs web server on specified port: default 80
    -k, --knowledge    | uses knowledge from KnowledgeTag
    -t, --transformer  | uses transformer from TransformerTag
    -r, --recursive    | uses files in recursive as input (ignored in web mode)
    -i, --input        | uses this input path (ignored in web mode)
    -o, --output       | dumps converted to this directory
}}}

 *
 * @author Philip Cali
 */
object Converter {
  def help = {
    println("usage: cct [--web] [--port=80] [--knowledge=blackboard[:package]] [-r] [--input=path] [--transformer=moodle[:package]] [--output=directory]")
    println("Options:")
    println("-h, --help %8s" format "prints out this help")
    println("-w, --web %8s" format "starts a web interface")
    println("-p, --port %8s" format "runs web server on specified port: default 80")
    println("-k, --knowledge %8s" format "uses knowledge from KnowledgeTag")
    println("-t, --transformer %8s" format "uses transformer from TransformerTag")
    println("-r, --recursive %8s" format "uses files in recursive as input (ignored in web mode)")
    println("-i, --input %8s" format "uses this input path (ignored in web mode)")
    println("-o, --output %8s" format "dumps converted to this directory")
  }

  def parseArgs(args: List[String], parsed: Map[String, String] = Map[String,String]()): Map[String, String] = {
    args match {
      case h :: value :: rest if(!value.startsWith("-") && !h.contains("=")) => 
        parsed + (translateKey(h) -> value) ++ parseArgs(rest)
      case h :: rest if(h contains("=")) => {
        val split = h.split("=")
        parsed + (translateKey(split(0)) -> split(1)) ++ parseArgs(rest)
      }
      case h :: rest => parsed + (translateKey(h) -> "true") ++ parseArgs(rest)
      case Nil => Map()
    }
  }

  def translateKey(key: String) = {
    val option = """(-\w)""".r
    option.findFirstIn(key).get match {
      case "-w" => "web"
      case "-t" => "transformer"
      case "-k" => "knowledge"
      case "-i" => "input"
      case "-p" => "port"
      case "-r" => "recursive"
      case "-o" => "output"
      case "-h" => "help"
      case _ => "unknown"
    }
  }

  def validate(config: Map[String, String]) = {
    // Preliminary checks
    if(config.contains("recursive") && !config.contains("input"))
      throw new IllegalArgumentException("Please provide an input path to recurse")
    if(!config.contains("web") && !config.contains("input"))
      throw new IllegalArgumentException("Please provide an input path")
    if(config.contains("web") && !config.contains("output"))
      throw new IllegalArgumentException("Please provide a output directory for your website") 
 
    // Input validation 
    if(config.contains("input")) {
      val input = new java.io.File(config("input"))

      if(!input.exists)
        throw new IllegalArgumentException("%s does not exists." format input.getAbsolutePath)
      if(config.contains("recursive") && !input.isDirectory)
        throw new IllegalArgumentException("%s is not a directory." format input.getAbsolutePath)
      else if(!config.contains("recursive") && !input.isFile)
        throw new IllegalArgumentException("%s is not a file." format input.getAbsolutePath)
    }

    // Output validation
    if(config.contains("output")) {
      val out = new java.io.File(config("output"))
      if(!out.exists)
        throw new IllegalArgumentException("%s output dir does not exists." format out.getAbsolutePath)
      if(out.isFile)
        throw new IllegalArgumentException("%s output specified is a file." format out.getAbsolutePath)
    }
  }

  def convert(path: String, output: String, kName: String, tName: String) = {
    val values = converterValues(kName, tName)
    val (k, kPacks) = values(0)
    val (t, tPacks) = values(1)

    println("Converting %s using %s to %s" format (path, k, t))
    val knowledge = MetaFinder.knowledge(k, kPacks, path)
    val transformer = MetaFinder.transformer(t, tPacks, knowledge.working, output)
    transformer.transform(knowledge.make)
  }

  def converterValues(kName: String, tName: String) = {
    List(kName, tName).map { name =>
      if(name.contains(":")) {
        val s = name.split(":")
        (s(0), s(1))
      } 
      else (name, "com.philipcali.cct")
    }
  }

  def exitCond(cond: => Boolean) = {
    if(cond) {
      help
      exit(0)
    }
  }

  def converters(typ: String) {
      println("Known %s: " format(typ))
      MetaFinder.tags.filter(_.conversion == typ).foreach { tag =>
        println("%s:%s %s" format(tag.name, tag.getClass.getPackage.getName, tag.description))
      }
  }

  def main(args: Array[String]) = {
    // Provide at least an input
    exitCond(args.size < 1)

    val config = parseArgs(args.toList)
    exitCond(config.contains("unknown") || config.contains("help"))

    // Let the program blow up is they passed in something that doesn't work
    try {
      val knowledgeName = config.get("knowledge").getOrElse("blackboard")
      val transformerName = config.get("transformer").getOrElse("moodle")

      if(knowledgeName.equalsIgnoreCase("list")) {
        converters("Knowledge")
      }

      if(transformerName.equalsIgnoreCase("list")) {
        converters("Transformer")
      }

      // Validate the config
      validate(config)

      val out = config.get("output").getOrElse("./")

      // Prep the knowledge and transformer values
      val values = converterValues(knowledgeName, transformerName)
      val knowledgeTag = MetaFinder.knowledgeTag(values(0)._1, values(0)._2)
      val transformerTag = MetaFinder.transformerTag(values(1)._1, values(1)._2)

      println(knowledgeTag.description)
      println(transformerTag.description)

      if(config.contains("web")) {
        // start the web server
        val port = config.get("port").getOrElse("80").toInt
        val downloads = new java.io.File(out).toURI

        Http(port).resources(downloads.toURL).filter(Planify {
          case GET(Path("/converter") & Accepts.Html(r)) => {
              Status(200) ~> 
              ContentType("text/html") ~> 
              Scalate(r, "index.ssp", ("knowledge", knowledgeTag), ("transformer", transformerTag))
          }
          case POST(Path("/upload") & MultiPart(req)) => 
            MultiPartParams.Disk(req).files("archive")  match {
              case fi :: rest => {
                // Write temp file
                val tempFile = new java.io.File("./" + fi.name)
                fi.write(tempFile)
                // Apply Conversion
                convert(tempFile.getAbsolutePath, out, knowledgeName, transformerName)
                // Remove Uploaded file
                tempFile.delete
                // Grab newly converted file of same name
                val newFile = new java.io.File(out).listFiles.find { 
                  f => f.getName.startsWith(tempFile.getName.split("\\.")(0))
                }.get
                Status(200) ~>
                ContentType("text/html") ~>
                ResponseString("<a href=\"" + newFile.getName + "\">"+ newFile.getName + "</a>")
              }
              case Nil => ResponseString("No File")
            }
        }).run

      } else if (config.contains("recursive")) {
        val folder = new java.io.File(config("input"))

        // Traverses all sub folders
        recurse(folder) { file =>
          if(file.getName.contains(".zip"))
            convert(file.getAbsolutePath, out, knowledgeName, transformerName)
        }
      } else {
        // Process a single file
        convert(config("input"), out, knowledgeName, transformerName)
      }
   
      // Cleanup temp dir
      remove("temp")
 
    } catch {
      case e: Exception => println(e.toString)
    }
  }
}
