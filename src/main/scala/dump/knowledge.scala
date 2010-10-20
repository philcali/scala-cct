package com.philipcali.cct
package dump

import system.KnowledgeTag
import knowledge.Knowledge
import Zip.extract
import course._

class DumpKnowledgeTag extends KnowledgeTag {
  def name = "dump"
  def version = "1.0"
}

object DumpKnowledge extends DumpKnowledgeTag {
  def tag = new DumpKnowledgeTag
  def apply(archive: String) = new DumpKnowledge(archive)
}

class DumpKnowledge(val archive: String) extends Knowledge {
  val working = "temp/" + archive.split("/").last.split("\\.").head

  def traverse[A](node: scala.xml.NodeSeq, level: Int=0) = {
    for(module <- node \ "MODULES" \ "MODULE") yield {
      CourseModule(handleModule(module), level, traverse(module, level + 1))
    }
  }
 
  def source = scala.xml.XML.load(working + "/dump.xml")

  def header = {
    val info = source \\ "INFO"
    CourseHeader(info \ "TITLE" text, info \ "DESCRIPTION" text)
  }

  lazy val nondisplay = for(module <- source \\ "NONDISPLAY" \ "MODULE")
    yield (handleModule(module))

  def make = {
    extract(archive, "temp")

    new Course(header, traverse(source \\ "ORGANIZATION"), nondisplay)
  }

  def handleModule(xml: scala.xml.Node) = xml \ "TYPE" text match {
    case "Section" => SectionRep(xml)
    case "Label" => LabelRep(xml)
    case "SingleFile" => SingleFileRep(xml)
    case "Directory" => DirectoryRep(xml)
    case "ExternalLink" => ExternalLinkRep(xml)
    case "OnlineDocument" => OnlineDocumentRep(xml)
    case "Quiz" => QuizRep(xml)
    case "StaffInformation" => StaffInformationRep(xml)
    case "QuestionCategory" => QuestionCategoryRep(xml)
    case _ => LabelRep(xml)
  }
}

