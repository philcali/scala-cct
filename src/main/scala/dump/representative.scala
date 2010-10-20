package com.philipcali.cct
package dump

import course._

case class ModuleData(id: Int,
                      name: String,
                      ref: String,
                      module: scala.xml.Node)

case class QuestionData(id: Int, 
                        name: String, 
                        grade: Double, 
                        text: String, 
                        question: scala.xml.Node)

object ModRep {
  def apply(node: scala.xml.Node) = {
    ((node \ "ID" text).toInt, node \ "NAME" text, node \ "REFERENCE" text)
  }
}

trait ModuleRep {
  def apply(node: scala.xml.Node) = {
    val (id, name, ref) = ModRep(node)
    build(ModuleData(id, name, ref, node))
  }

  def build(moduledata: ModuleData): Module
}

trait FileRep {
  def fileRep(file: scala.xml.Node) = {
    val name = (file \ "NAME").text
    val linkname = (file \ "LINKNAME").text
    val size = (file \ "SIZE").text.toLong
    File(name, linkname, size)
  }
}

// Here starts the higher module type representatives
object SectionRep extends ModuleRep {
  def build(data: ModuleData) = new Section(data.name)
}

object LabelRep extends ModuleRep {
  def build(labelData: ModuleData) = {
    new Label(labelData.id, labelData.name, labelData.ref)
  }
}

object SingleFileRep extends ModuleRep with FileRep {
  def build(data: ModuleData) = {
    new SingleFile(data.id, data.name, data.ref, fileRep((data.module \ "FILE")(0)))
  }
}

object DirectoryRep extends ModuleRep with FileRep {
  def build(data: ModuleData) = {
    val files = for(file <- data.module \\ "FILE") yield(fileRep(file))
    new Directory(data.id, data.name, data.ref, files.toList)
  }
}

object OnlineDocumentRep extends ModuleRep {
  def build(data: ModuleData) = {
    new OnlineDocument(data.id, data.name, data.ref, data.module \ "TEXT" text)
  }
}

object ExternalLinkRep extends ModuleRep {
  def build(data: ModuleData) = {
    new ExternalLink(data.id, data.name, data.ref, data.module \ "LINK" text)
  }
}

object StaffInformationRep extends ModuleRep {
  def contactRep(contact: scala.xml.Node) = {
    Contact(contact \ "TITLE" text,
            contact \ "GIVEN" text,
            contact \ "FAMILY" text,
            contact \ "EMAIL" text,
            contact \ "PHONE" text,
            contact \ "OFFICE" text,
            contact \ "IMAGE" text)
  }

  def build(data: ModuleData) = {
    new StaffInformation(data.id, data.name, data.ref, contactRep((data.module \ "CONTACT")(0)))
  }
}

object QuizRep extends ModuleRep {
  def build(data: ModuleData) = {
    new Quiz(data.id, 
             data.name, 
             data.ref, 
             data.module \\ "INFO" text, 
             QuestionCategoryRep(data.module).asInstanceOf[QuestionCategory])
  }
}

object QuestionCategoryRep extends ModuleRep {
  def questions(module: scala.xml.Node) = {
    for(question <- module \\ "QUESTIONS" \ "QUESTION") yield(handleQuestion(question))
  }

  def handleQuestion(question: scala.xml.Node) = question \ "TYPE" text match {
    case "MultipleChoice" => MultichoiceRep(question)
    case "Essay" => EssayRep(question)
    case "Matching" => MatchingRep(question)
    case "BooleanQuestion" => BooleanQuestionRep(question)
    case "Ordering" => OrderingRep(question)
    case "FillInBlank" => FillInBlankRep(question)
    case "Numeric" => NumericRep(question)
  }

  def build(data: ModuleData) = {
    new QuestionCategory(data.id, 
                         data.name, 
                         data.ref, 
                         data.module \\ "INFO" text,
                         questions(data.module))
  }
}

// Here starts the question type representatives
trait QuestionRep {
  def answerReps(answers: scala.xml.Node) = {
    for(answer <- answers \\ "ANSWER") yield {
      val id = (answer \ "ID" text).toInt
      val name = (answer \ "NAME" text) 
      val weight = (answer \ "WEIGHT" text).toDouble
      val feedback = (answer \ "FEEDBACK" text)
      Answer(id, name, weight, feedback)
    }
  }

  def apply(question: scala.xml.Node) = {
    val (id, name, ref) = ModRep(question)
    build(QuestionData(id, name, (question \ "GRADE" text).toDouble, (question \ "TEXT" text), question))
  }

  def build(questiondata: QuestionData): Question
}

object MultichoiceRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with MultipleChoice {
      def answers = answerReps(data.question)

      override def incorrectFeedback = data.question \\ "INCORRECTFEEDBACK" text
      override def correctFeedback = data.question \\ "CORRECTFEEDBACK" text
    }
  }
}

object BooleanQuestionRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with BooleanQuestion {
      def answers = answerReps(data.question)

      override def trueAnswer = (data.question \\ "TRUE" text).toInt
      override def falseAnswer = (data.question \\ "FALSE" text).toInt
    }
  }
}

object EssayRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Essay
  }
}

object MatchingRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Matching {
      def answers = answerReps(data.question)
    }
  }
}

object OrderingRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Ordering {
      def answers = answerReps(data.question)
    }
  }  
}

object FillInBlankRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with FillInBlank {
      def answers = answerReps(data.question)
    }
  }
}

object NumericRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Numeric {
      def answers = answerReps(data.question)
      override def tolerance = (data.question \\ "TOLERANCE" text).toDouble
    }
  }
}
