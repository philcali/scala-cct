package com.philipcali.cct
package dump

import system.{KnowledgeTag, Embed}
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

class DumpKnowledge(val archive: String) extends Knowledge with Embed {
  val embedPattern = defaults.r
 
  val working = "temp/" + archive.split("/").last.split("\\.").head

  def traverse[A](node: scala.xml.NodeSeq) = {
  }
 
  def source = scala.xml.XML.load(working + "/dump.xml")

  def header = {
    val info = source \\ "INFO"
    CourseHeader(info \ "TITLE" text, info \ "DESCRIPTION" text)
  }

  val nondisplay = for(module <- source \\ "NONDISPLAY" \ "MODULE")
    yield {
    module \ "TYPE" text match {
      case "QuestionCategory" => handleCategories(module)
      case _ => handleUnknown(module \ "REFERENCE" text)
    }
  }

  def handleCategories(node: scala.xml.Node) = {
    val (id, name, ref) = ModRep(node) 
    new QuestionCategory(id, name, ref, node \ "INFO" text, questions(node))
  }

  def questions(category: scala.xml.Node) = {
    for(question <- category \\ "QUESTIONS" \ "QUESTION") yield {
      question \ "TYPE" text match {
        case "MultipleChoice" => MultichoiceRep(question)
      }
    }
  }

  def make = {
    extract(archive, "temp")

    new Course(header, traverse(source \\ "ORGANIZATION"), nondisplay)
  } 
}

object ModRep {
  def unapply(node: scala.xml.Node) = {
    ((node \ "ID" text).toInt, node \ "NAME" text, node \ "REFERENCE" text)
  }
}

case class QuestionData(id: Int, name: String, ref: String, text: String, question: scala.xml.Node)

trait QuestionRep {
  def unapply(quesiton: scala.xml.Node) = {
    val (id, name, ref) = ModRep(question)
    build(QuestionData(id, name, ref, question \ "TEST" text, question))
  }

  def build(questiondata: QuestionData): Question
}

object MultichoiceRep extends QuestionRep {
  def build(questiondata: QuestionData) = {
    new Question(
  }
}
