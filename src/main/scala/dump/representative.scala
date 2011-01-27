package com.philipcali.cct
package dump

import course._

/**
 * @author Philip Cali
 **/
case class ModuleData(id: Int,
                      name: String,
                      ref: String,
                      module: scala.xml.Node)

/**
 * @author Philip Cali
 **/
case class QuestionData(id: Int, 
                        name: String, 
                        grade: Double, 
                        text: String, 
                        question: scala.xml.Node)

/**
 * @author Philip Cali
 **/
object ModRep {
  def apply(node: scala.xml.Node) = {
    ((node \ "ID" text).toInt, node \ "NAME" text, node \ "REFERENCE" text)
  }
}

/**
 * @author Philip Cali
 **/
trait ModuleRep {
  def apply(node: scala.xml.Node) = {
    val (id, name, ref) = ModRep(node)
    build(ModuleData(id, name, ref, node))
  }

  def build(moduledata: ModuleData): Module
}

/**
 * @author Philip Cali
 **/
trait FileRep {
  def fileRep(file: scala.xml.Node) = {
    val name = (file \ "NAME").text
    val linkname = (file \ "LINKNAME").text
    val size = (file \ "SIZE").text.toLong
    File(name, linkname, size)
  }
}

// Here starts the higher module type representatives
/**
 * @author Philip Cali
 **/
object SectionRep extends ModuleRep {
  def build(data: ModuleData) = new Section(data.name)
}

/**
 * @author Philip Cali
 **/
object LabelRep extends ModuleRep {
  def build(labelData: ModuleData) = {
    new Label(labelData.id, labelData.name, labelData.ref)
  }
}

/**
 * @author Philip Cali
 **/
object SingleFileRep extends ModuleRep with FileRep {
  def build(data: ModuleData) = {
    new SingleFile(data.id, data.name, data.ref, fileRep((data.module \ "FILE")(0)))
  }
}

/**
 * @author Philip Cali
 **/
object DirectoryRep extends ModuleRep with FileRep {
  def build(data: ModuleData) = {
    val files = for(file <- data.module \\ "FILE") yield(fileRep(file))
    new Directory(data.id, data.name, data.ref, files.toList)
  }
}

/**
 * @author Philip Cali
 **/
object OnlineDocumentRep extends ModuleRep {
  def build(data: ModuleData) = {
    new OnlineDocument(data.id, data.name, data.ref, data.module \ "TEXT" text)
  }
}

/**
 * @author Philip Cali
 **/
object ExternalLinkRep extends ModuleRep {
  def build(data: ModuleData) = {
    new ExternalLink(data.id, data.name, data.ref, data.module \ "LINK" text)
  }
}

/**
 * @author Philip Cali
 **/
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

/**
 * @author Philip Cali
 **/
object QuizRep extends ModuleRep {
  def build(data: ModuleData) = {
    new Quiz(data.id, 
             data.name, 
             data.ref, 
             data.module \\ "INFO" text, 
             QuestionCategoryRep(data.module).asInstanceOf[QuestionCategory])
  }
}

/**
 * @author Philip Cali
 **/
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
/**
 * @author Philip Cali
 **/
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

/**
 * @author Philip Cali
 **/
object MultichoiceRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with MultipleChoice {
      def answers = answerReps(data.question)

      override def incorrectFeedback = data.question \\ "INCORRECTFEEDBACK" text
      override def correctFeedback = data.question \\ "CORRECTFEEDBACK" text
    }
  }
}

/**
 * @author Philip Cali
 **/
object BooleanQuestionRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with BooleanQuestion {
      def answers = answerReps(data.question)

      override def trueAnswer = (data.question \\ "TRUE" text).toInt
      override def falseAnswer = (data.question \\ "FALSE" text).toInt
    }
  }
}

/**
 * @author Philip Cali
 **/
object EssayRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Essay
  }
}

/**
 * @author Philip Cali
 **/
object MatchingRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Matching {
      def answers = answerReps(data.question)
    }
  }
}

/**
 * @author Philip Cali
 **/
object OrderingRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Ordering {
      def answers = answerReps(data.question)
    }
  }  
}

/**
 * @author Philip Cali
 **/
object FillInBlankRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with FillInBlank {
      def answers = answerReps(data.question)
    }
  }
}

/**
 * @author Philip Cali
 **/
object NumericRep extends QuestionRep {
  def build(data: QuestionData) = {
    new Question(data.id, data.name, data.text, data.grade) with Numeric {
      def answers = answerReps(data.question)
      override def tolerance = (data.question \\ "TOLERANCE" text).toDouble
    }
  }
}
