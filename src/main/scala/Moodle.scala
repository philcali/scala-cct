package com.philipcali.cct.moodle

import com.philipcali.cct.course._
import java.util.Date

class MoodleTransformer(val working: String) {
  def infoMods = Map("label" -> ((m: Module) => m.isInstanceOf[Label]),
                     "resource" -> ((m: Module) => m.isInstanceOf[Resource]),
                     "quiz" -> ((m: Module) => m.isInstanceOf[Quiz]))

  def transform(course: Course) = {
    
    implicit def module2MoodleModule(m: Module) = m match {
      case category: QuestionCategory => new MoodleQuestionCateogry(category)
      case label: Label => new MoodleLabel(label)
      case file: SingleFile => new MoodleSingleFile(file)
      case dir: Directory => new MoodleDirectory(dir)
      case online: OnlineDocument => new MoodleOnlineText(online)
      case link: ExternalLink => new MoodleExternalLink(link)
      case quiz: Quiz => new MoodleQuiz(quiz, 
                  course.nondisplay.find(_.name == quiz.name).get.asInstanceOf[QuestionCategory])
      case _ => new MoodleLabel(m)
    }

    <MOODLE_BACKUP>
      <INFO>
        <NAME>backup-xxx.zip</NAME>
        <MOODLE_VERSION>2007101501</MOODLE_VERSION>
        <MOODLE_RELEASE>1.8</MOODLE_RELEASE>
        <BACKUP_VERSION>2007101000</BACKUP_VERSION>
        <BACKUP_RELEASE>1.8</BACKUP_RELEASE>
        <DATE>{ new Date().getTime }</DATE>
        <ORIGINAL_WWWROOT>http://philcalicode.blogspot.com</ORIGINAL_WWWROOT>
        <ZIP_METHOD>internal</ZIP_METHOD>
        <DETAILS>
            { infoMods.map { tupes =>
                val instances = course.details.filter(tupes._2)
                if(instances.size > 0) {
                  <MOD>
                    <NAME>{ tupes._1 }</NAME>
                    <INCLUDED>true</INCLUDED>
                    <USERINFO>false</USERINFO>
                    <INSTANCES> {
                       instances map { instance =>
                        <INSTANCE>
                        <ID>{ instance.id }</ID>
                        <NAME>{ instance.name }</NAME>
                        <INCLUDED>true</INCLUDED>
                        <USERINFO>false</USERINFO>
                        </INSTANCE>
                        }
                     }
                    </INSTANCES>
                  </MOD>
                 }
                else ""
              }
            }
          <METACOURSE>false</METACOURSE>
          <USERS>none</USERS>
          <LOGS>false</LOGS>
          <USERFILES>false</USERFILES>
          <COURSEFILES>true</COURSEFILES>
          <SITEFILES>true</SITEFILES>
          <GRADEBOOKHISTORIES>false</GRADEBOOKHISTORIES>
          <MESSAGES>false</MESSAGES>
          <BLOGS>false</BLOGS>
        </DETAILS>
      </INFO>
      <ROLES></ROLES>
      <COURSE>
        <HEADER>
          <ID></ID>
          <CATEGORY>
            <ID>1</ID>
            <NAME>Miscellaneous</NAME>
          </CATEGORY>
          <PASSWORD></PASSWORD>
          <FULLNAME>{ course.info.title }</FULLNAME>
          <SHORTNAME>{ course.info.title.split(" ").mkString }</SHORTNAME>
          <IDNUMBER></IDNUMBER>
          <SUMMARY>{ course.info.description }</SUMMARY>
          <FORMAT>topics</FORMAT>
          <SHOWGRADES>1</SHOWGRADES>
          <NEWSITEMS>1</NEWSITEMS>
          <TEACHER>Teacher</TEACHER>
          <TEACHERS>Teachers</TEACHERS>
          <STUDENT>Student</STUDENT>
          <STUDENTS>Students</STUDENTS>
          <GUEST>0</GUEST>
          <STARTDATE>1282539600</STARTDATE>
          <NUMSECTIONS>5</NUMSECTIONS>
          <MAXBYTES>0</MAXBYTES>
          <SHOWREPORTS>0</SHOWREPORTS>
          <GROUPMODE>0</GROUPMODE>
          <GROUPMODEFORCE>0</GROUPMODEFORCE>
          <DEFAULTGROUPINGID>0</DEFAULTGROUPINGID>
          <LANG></LANG>
          <THEME></THEME>
          <COST></COST>
          <CURRENCY>USD</CURRENCY>
          <MARKER>0</MARKER>
          <VISIBLE>1</VISIBLE>
          <HIDDENSECTIONS>0</HIDDENSECTIONS>
          <TIMECREATED></TIMECREATED>
          <TIMEMODIFIED></TIMEMODIFIED>
          <METACOURSE>0</METACOURSE>
          <EXPIRENOTIFY>0</EXPIRENOTIFY>
          <NOTIFYSTUDENTS>0</NOTIFYSTUDENTS>
          <EXPIRYTHRESHOLD>864000</EXPIRYTHRESHOLD>
          <ENROLLABLE>1</ENROLLABLE>
          <ENROLSTARTDATE>0</ENROLSTARTDATE>
          <ENROLENDDATE>0</ENROLENDDATE>
          <ENROLPERIOD>0</ENROLPERIOD>
          <ROLES_OVERRIDES>
          </ROLES_OVERRIDES>
          <ROLES_ASSIGNMENTS>
          </ROLES_ASSIGNMENTS>
        </HEADER>
        <BLOCKS/>
        <SECTIONS>
          { course.sections.filter(_.children.size != 0).zipWithIndex.map { entry =>
              val (section, index) = entry
              <SECTION>
                <ID>{ index + 2 }</ID>
                <NUMBER>{ index + 1 }</NUMBER>
                <SUMMARY>{ section.wrapped.name }</SUMMARY>
                <VISIBLE>1</VISIBLE>
                <MODS>
                  { course.mods(section).zipWithIndex.map { modEntry =>
                     val (mod, modID) = modEntry
                     <MOD>
                      <ID>{ index + mod.wrapped.id + modID }</ID>
                      <TYPE>{ determine(mod.wrapped) }</TYPE>
                      <INSTANCE>{ mod.wrapped.id }</INSTANCE>
                      <ADDED/>
                      <SCORE>0</SCORE>
                      <INDENT>{ mod.level - 1 }</INDENT>
                      <VISIBLE>1</VISIBLE>
                      <GROUPMODE>0</GROUPMODE>
                      <GROUPINGID>0</GROUPINGID>
                      <GROUPMEMBERSONLY>0</GROUPMEMBERSONLY>
                      <IDNUMBER>$@NULL@$</IDNUMBER>
                      <ROLES_OVERRIDES>
                      </ROLES_OVERRIDES>
                      <ROLES_ASSIGNMENTS>
                      </ROLES_ASSIGNMENTS>
                    </MOD>
                    }
                  }
                </MODS>
              </SECTION>
            } 
          }
        </SECTIONS>
        <QUESTION_CATEGORIES>
          { course.nondisplay.filter(_.isInstanceOf[QuestionCategory]).map { category =>
             category.toXML 
            } 
          }
        </QUESTION_CATEGORIES>
        <GROUPS/>
        <GRADEBOOK/>
        <MODULES>
          { course.details.filter(m => !m.isInstanceOf[Section]).map { mod =>
              mod.toXML
            }
          }
        </MODULES>
        <FORMATDATA />
      </COURSE>
    </MOODLE_BACKUP>
  }

  def determine(m: Module) = m match {
    case l: Label => "label"
    case r: Resource => "resource"
    case q: Quiz => "quiz"
    case _ => "label"
  }

}

abstract class MoodleModule(val under: Module) {
  def modType: String
  def toXML: scala.xml.Elem
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

class MoodleOnlineText(under: OnlineDocument) extends MoodleModule(under) with MoodleResource {
  def tpe = "text"
  override def referfence = "2"
  override def text = under.text
}

class MoodleSingleFile(under: SingleFile) extends MoodleModule(under) with MoodleResource {
  def tpe = "file"
  override def referfence = under.file.linkname
}

class MoodleDirectory(under: Directory) extends MoodleModule(under) with MoodleResource {
  def tpe = "directory"
  override def referfence = under.name.split(" ").mkString("_")
}

class MoodleExternalLink(under: ExternalLink) extends MoodleModule(under) with MoodleResource {
  def tpe = "file"
  override def referfence = under.url
}

class MoodleLabel(under: Module) extends MoodleModule(under) {
  def modType = "label"
 
  def toXML = {
    <MOD>
      <ID>{ under.id }</ID>
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

class MoodleQuiz(under: Quiz, category: QuestionCategory) extends MoodleModule(under) {
  def modType = "quiz"

  def toXML = {
    <MOD>
      <ID>{ under.id }</ID>
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
      <QUESTIONS>{ category.questions.map(_.id).mkString(",") }</QUESTIONS>
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
        { category.questions.map { question =>
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
