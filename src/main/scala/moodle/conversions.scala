package com.philipcali.cct
package moodle

import system.Embed
import course._
import Utils._
import java.io.File
import grizzled.util.{withCloseable => withc}
import scala.io.Source.{fromFile => open}

object MoodleConversions {
  implicit def module2MoodleModule(m: Module) = m match {
    case category: QuestionCategory => new MoodleQuestionCateogry(category)
    case label: Label => new MoodleLabel(label)
    case file: SingleFile => new MoodleSingleFile(file)
    case dir: Directory => new MoodleDirectory(dir)
    case online: OnlineDocument => new MoodleOnlineText(online)
    case link: ExternalLink => new MoodleExternalLink(link)
    case quiz: Quiz => new MoodleQuiz(quiz) 
    case staff: StaffInformation => new MoodleStaff(staff)
    case _ => new MoodleLabel(m)
  }
}

abstract class MoodleModule(val under: Module) {
  def modType: String
  def toXML: scala.xml.Elem

  def dirname = dirclean(under.name)

  def transform(working: String, staging: String) {
    val oldPath = working + "/" + under.from
    val oldDir = new File(oldPath)
    
    if(oldDir.exists) {
      val newPath = staging + "/course_files/" + dirname
      val newFile = new File(newPath)
      newFile.mkdir

      copy(oldDir, newFile)
    }

  }
}

trait MoodleResource extends MoodleModule {
  def tpe: String
  def modType = "resource"
  def referfence = ""
  def text = ""

  def toXML = {
    <MOD>
      <ID>{ under.id }</ID>
      <MODTYPE>{ modType }</MODTYPE>
      <NAME>{ under.name }</NAME>
      <TYPE>{ tpe }</TYPE>
      <REFERENCE>{ referfence }</REFERENCE>
      <SUMMARY>{ under.name }</SUMMARY>
      <ALLTEXT>{ text }</ALLTEXT>
      <POPUP></POPUP>
      <OPTIONS></OPTIONS>
      <TIMEMODIFIED />
    </MOD>
  }
}

class MoodleOnlineText(under: OnlineDocument) extends MoodleModule(under) with MoodleResource with Embed {
  val embedPattern = defaults.r

  def tpe = "text"
  override def referfence = "1"
  
  override def text = {
    embedded(under.text, 
             """\$@FILEPHP@\$\$@SLASH@\$""" + dirname + "/")
  }
}

class MoodleSingleFile(under: SingleFile) extends MoodleModule(under) with MoodleResource {
  def tpe = "file"
  override def referfence = dirname + "/" + under.file.linkname
}

class MoodleDirectory(under: Directory) extends MoodleModule(under) with MoodleResource {
  def tpe = "directory"
  override def referfence = dirname 
}

class MoodleExternalLink(under: ExternalLink) extends MoodleModule(under) with MoodleResource {
  def tpe = "file"
  override def referfence = under.url
}

class MoodleStaff(under: StaffInformation) extends MoodleModule(under) with MoodleResource {
  def tpe = "html"
  override def dirname = "staff_information"
  override def referfence = ""
  override def text = {
    def html = {
      val Contact(title, given, family, email, phone, office, image) = under.contact
      val fullname = List(title, given, family) mkString " "

      val img = if(image != "") <img src={ "$@FILEPHP@$$@SLASH@$" + dirname + "/" + image }/> else ""

      <h1 style="text-align: center">{ fullname }</h1>
      <table>
        <tr>
          <td>{ img }</td>
          <td>
            <ul style="list-style-type: none;">
              <li>Name: { fullname }</li>
              <li>Email: { email }</li>
              <li>Phone: { phone }</li>
              <li>Office: { office }</li>
            </ul>
          </td>
        </tr>
      </table>
    }

    html.mkString
  }
}

class MoodleLabel(under: Module) extends MoodleModule(under) {
  def modType = "label"
 
  def toXML = {
    <MOD>
      <ID>{ under.id }</ID>
      <MODTYPE>{ modType }</MODTYPE>
      <NAME>{ under.name }</NAME>
      <CONTENT>{ under.name }</CONTENT>
      <TIMEMODIFIED />
    </MOD>
  } 
}

class MoodleQuestionCateogry(under: QuestionCategory) extends MoodleModule(under) {
  def modType = "course"

  implicit def question2MoodleQuestion(question: Question) = question match {
    case matching: Matching => new MoodleMatchingQuestion(matching)
    case multiple: MultipleChoice => new MoodleMultipleChoice(multiple)
    case boolean: BooleanQuestion => new MoodleTrueFalse(boolean)
    case numeric: Numeric => new MoodleNumeric(numeric)
    case shortAnswer: FillInBlank => new MoodleShortAnswer(shortAnswer)
    case essay: Essay => new MoodleEssay(essay)
  }

  def toXML = {
    <QUESTION_CATEGORY>
      <ID>{ under.id }</ID>
      <NAME>{ under.name }</NAME>
      <INFO>{ under.info }</INFO>
      <CONTEXT>
        <LEVEL>{ modType }</LEVEL>
      </CONTEXT>
      <STAMP>{ under.id }</STAMP>
      <PARENT>0</PARENT>
      <SORTORDER>999</SORTORDER>
      <QUESTIONS>
        { under.questions.map { _.toXML } }
      </QUESTIONS>
    </QUESTION_CATEGORY>
  }
}

class MoodleNumeric(under: Numeric) extends MoodleModule(under) with MoodleQuestion {
  def modType = "numerical"

  def toXML = {
    baseXML {
      <NUMERICAL>
        <ANSWER>
          <ID>1</ID>
          <TOLERANCE>{ under.tolerance }</TOLERANCE>
        </ANSWER>
      </NUMERICAL>
      <ANSWERS>
        <ANSWER>
          <ID>1</ID>
          <ANSWER_TEXT>{ under.answers.head.text }</ANSWER_TEXT>
          <FRACTION>1</FRACTION>
          <FEEDBACK>{ under.answers.head.feedback }</FEEDBACK>
        </ANSWER>
      </ANSWERS>
    }
  }
}

class MoodleShortAnswer(under: FillInBlank) extends MoodleModule(under) with MoodleQuestion {
  def modType = "shortanswer"
  
  def toXML = {
    baseXML {
      <SHORTANSWER>
        <ANSWERS>{ under.answers.map(_.id).mkString(",") }</ANSWERS>
        <USECASE>0</USECASE>
      </SHORTANSWER>
      <ANSWERS>
        { under.answers.map { answer =>
            <ANSWER>
              <ID>{ answer.id }</ID>
              <ANSWER_TEXT>{ answer.text }</ANSWER_TEXT>
              <FRACTION>{ answer.weight }</FRACTION>
              <FEEDBACK>{ answer.feedback }</FEEDBACK>   
            </ANSWER>
          }
        }
      </ANSWERS>
    }
  }
}

class MoodleTrueFalse(under: BooleanQuestion) extends MoodleModule(under) with MoodleQuestion {
  def modType = "truefalse"
  
  def toXML = {
    baseXML {
    <TRUEFALSE>
      <TRUEANSWER>1</TRUEANSWER>
      <FALSEANSWER>2</FALSEANSWER>
    </TRUEFALSE>
    <ANSWERS>
      { under.answers.zipWithIndex.map { entry =>
          val (answer, id) = entry
          <ANSWER>
            <ID>{ id + 1 }</ID>
            <ANSWER_TEXT>{ answer.text }</ANSWER_TEXT>
            <FRACTION>{ answer.weight }</FRACTION>
            <FEEDBACK>{ answer.feedback}</FEEDBACK>
          </ANSWER>
        }
      }
    </ANSWERS>
    }
  }
}

class MoodleEssay(under: Essay) extends MoodleModule(under) with MoodleQuestion {
  def modType = "essay"
  
  def toXML = {
    baseXML {
    <ANSWERS>
      { under.answers.map { answer =>
      <ANSWER>
        <ID>{ answer.id }</ID>
        <ANSWER_TEXT/>
        <FRACTION>0</FRACTION>
        <FEEDBACK/>
      </ANSWER>
        }
      }
    </ANSWERS>
    }
  }
}

class MoodleMultipleChoice(under: MultipleChoice) extends MoodleModule(under) with MoodleQuestion {
  def modType = "multichoice"
  
  // Single and shuffle
  def toXML = {
    baseXML {
    <MULTICHOICE>
      <LAYOUT>0</LAYOUT>
      <ANSWERS>{ under.answers.map(_.id).mkString(",") }</ANSWERS>
      <SINGLE>1</SINGLE>
      <SHUFFLEANSWERS>0</SHUFFLEANSWERS>
      <CORRECTFEEDBACK>{ under.correctFeedback }</CORRECTFEEDBACK>
      <PARITALLYCORRECTFEEDBACK />
      <INCORRECTFEEDBACK>{ under.incorrectFeedback }</INCORRECTFEEDBACK>
    </MULTICHOICE>
    <ANSWERS>
      { under.answers.map { answer =>
          <ANSWER>
            <ID>{ answer.id }</ID>
            <ANSWER_TEXT>{ answer.text }</ANSWER_TEXT>
            <FRACTION>{ answer.weight }</FRACTION>
            <FEEDBACK>{ answer.feedback }</FEEDBACK>
          </ANSWER>
        }
      }
    </ANSWERS>
    }
  }
}

class MoodleMatchingQuestion(under: Matching) extends MoodleModule(under) with MoodleQuestion {
  def modType = "match"

  def toXML = {
    baseXML {
    <MATCHS>
      { under.answers.map { answer =>
          <MATCH>
            <ID>{ answer.id }</ID>
            <CODE/>
            <QUESTIONTEXT>{ answer.text }</QUESTIONTEXT>
            <ANSWERTEXT>{ answer.feedback }</ANSWERTEXT>
          </MATCH>
        }
      }
    </MATCHS>
    }
  }
}

trait MoodleQuestion extends MoodleModule {
  
  def baseXML[A](middle: => A) = {
    <QUESTION>
      <ID>{ under.id }</ID>
      <PARENT>0</PARENT>
      <NAME>{ under.name }</NAME>
      <QUESTIONTEXT>{ under.asInstanceOf[Question].text }</QUESTIONTEXT>
      <QUESTIONTEXTFORMAT>0</QUESTIONTEXTFORMAT>
      <IMAGE/>
      <GENERALFEEDBACK/>
      <DEFAULTGRADE>{ under.asInstanceOf[Question].grade }</DEFAULTGRADE>
      <PENALTY>0</PENALTY>
      <QTYPE>{ modType }</QTYPE>
      <LENGTH>1</LENGTH>
      <STAMP>{ under.id }</STAMP>
      <VERSION>{ under.id + "version" }</VERSION>
      <HIDDEN>0</HIDDEN>
      <TIMECREATED/>
      <TIMEMODIFIED/>
      <CREATEDBY/>
      <MODIFIEDBY/>
      { middle }
    </QUESTION>
  }
}

class MoodleQuiz(under: Quiz) extends MoodleModule(under) {
  def modType = "quiz"

  def toXML = {
    <MOD>
      <ID>{ under.id }</ID>
      <MODTYPE>{ modType }</MODTYPE>
      <NAME>{ under.name }</NAME>
      <INTRO>{ under.description }</INTRO>
      <TIMEOPEN>0</TIMEOPEN>
      <TIMECLOSE>0</TIMECLOSE>
      <OPTIONFLAGS>1</OPTIONFLAGS>
      <PENALTYSCHEME>1</PENALTYSCHEME>
      <ATTEMPTS_NUMBER>0</ATTEMPTS_NUMBER>
      <ATTEMPTONLAST>0</ATTEMPTONLAST>
      <GRADEMETHOD>1</GRADEMETHOD>
      <DECIMALPOINTS>2</DECIMALPOINTS>
      <REVIEW></REVIEW>
      <QUESTIONSPERPAGE>0</QUESTIONSPERPAGE>
      <SHUFFLEQUESTIONS>0</SHUFFLEQUESTIONS>
      <SHUFFLEANSWERS>1</SHUFFLEANSWERS>
      <QUESTIONS>{ under.category.questions.map(_.id).mkString(",") }</QUESTIONS>
      <SUMGRADES>1</SUMGRADES>
      <GRADE>10</GRADE>
      <TIMECREATED></TIMECREATED>
      <TIMEMODIFIED></TIMEMODIFIED>
      <TIMELIMIT>0</TIMELIMIT>
      <PASSWORD></PASSWORD>
      <SUBNET></SUBNET>
      <POPUP>0</POPUP>
      <DELAY1>0</DELAY1>
      <DELAY2>0</DELAY2>
      <QUESTION_INSTANCES>
        { under.category.questions.map { question =>
            <QUESTION_INSTANCE>
              <ID>{ question.id }</ID>
              <QUESTION>{ question.id }</QUESTION>
              <GRADE>{ question.grade }</GRADE>
            </QUESTION_INSTANCE>
          } 
        }
      </QUESTION_INSTANCES>
      <FEEDBACKS/>
    </MOD>
  }
}
