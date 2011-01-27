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
package com.philipcali.cct
package dump

import system.{Tagger, KnowledgeTag}
import knowledge.Knowledge
import Zip.extract
import course._

/**
 * @author Philip Cali
 **/
class DumpKnowledgeTag extends KnowledgeTag {
  def name = "dump"
  def version = "1.0"
}

/**
 * @author Philip Cali
 **/
object DumpKnowledge extends Tagger[DumpKnowledgeTag] {
  def tag = new DumpKnowledgeTag
  def apply(archive: String) = new DumpKnowledge(archive)
}

/**
 * @author Philip Cali
 **/
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
