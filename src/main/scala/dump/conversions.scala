package com.philipcali.cct

package dump

import course._
import java.io.{File => JFile}
import Utils.copy

object DumpConversions {
  implicit def module2DumpModule(m: CourseModule): DumpModule = m.wrapped match {
    case l: Label => new DumpLabel(m, l)
    case s: SingleFile => new DumpSingleFile(m, s)
    case d: Directory => new DumpDirectory(m, d)
    case o: OnlineDocument => new DumpOnline(m, o)
    case e: ExternalLink => new DumpExternalLink(m, e)
    case staff: StaffInformation => new DumpStaffInformation(m, staff)
    case quiz: Quiz => new DumpQuiz(m, quiz)
    case section: Section => new DumpSection(m, section)
    case _ => new DumpUnknown(m, m.wrapped)
  }

  implicit def nondisplay2DumpModule(m: Module) = m match {
    case category: QuestionCategory => new DumpCategory(category)
    case _ => new DumpUnknown(CourseModule(m, 0, Nil), m)
  }
}

trait XMLOuput {
  def toXML: scala.xml.Node
  def extraXML = <EXTRA/>
}

trait DumpModule extends XMLOuput {
  import DumpConversions._

  val under: CourseModule
  val module: Module

  def tpe = module.getClass.getSimpleName

  def transform(working: String, staging: String) = {
    val oldDir = new JFile(working + "/" + module.from)
    if(oldDir.exists) {
      val newDir = new JFile(staging + "/" + module.from)

      copy(oldDir, newDir)
    }
  }

  def toXML = {
    <MODULE>
      <ID>{ module.id }</ID>
      <LEVEL>{ under.level }</LEVEL>
      <TYPE>{ tpe }</TYPE>
      <NAME>{ module.name }</NAME>
      <REFERENCE>{ module.from }</REFERENCE>
      { extraXML }
      <MODULES>{ under.children.map(_.toXML) }</MODULES>
    </MODULE>
  }
}

trait FileHandler {
  def fileXML(file: File) = {
    <FILE>
      <NAME>{ file.name }</NAME>
      <LINKNAME>{ file.linkname }</LINKNAME>
      <SIZE>{ file.size }</SIZE>
    </FILE>
  }  
}

class DumpUnknown(val under: CourseModule, val module: Module) extends DumpModule {
  override def tpe = "unsupported"
}

class DumpSection(val under: CourseModule, val module: Section) extends DumpModule 

class DumpLabel(val under: CourseModule,val module: Label) extends DumpModule

class DumpSingleFile(val under: CourseModule,val module: SingleFile) extends DumpModule with FileHandler{
  override def extraXML = fileXML(module.file)
}

class DumpDirectory(val under: CourseModule,val module: Directory) extends DumpModule with FileHandler {
  override def extraXML = {
    <FILES>{ module.directory.map(fileXML) }</FILES>
  }
}

class DumpOnline(val under: CourseModule, val module: OnlineDocument) extends DumpModule {
  override def extraXML = {
    <TEXT>{ module.text }</TEXT>
  }
}

class DumpExternalLink(val under: CourseModule, val module: ExternalLink) extends DumpModule {
  override def extraXML = {
    <LINK>{ module.url }</LINK>
  }
}

class DumpStaffInformation(val under: CourseModule, val module: StaffInformation) extends DumpModule{
  override def extraXML = {
    <CONTACT>
      <TITLE>{ module.contact.title }</TITLE>
      <GIVEN>{ module.contact.given }</GIVEN>
      <FAMILY>{ module.contact.family }</FAMILY>
      <EMAIL>{ module.contact.email }</EMAIL>
      <PHONE>{ module.contact.phone }</PHONE>
      <OFFICE>{ module.contact.office }</OFFICE>
      <IMAGE>{ module.contact.image }</IMAGE>
    </CONTACT>
  }
}

class DumpQuiz(val under: CourseModule, val module: Quiz) extends DumpModule {
  import DumpConversions.nondisplay2DumpModule  

  override def extraXML = {
    <EXTRA>
      <INFO>{ module.description }</INFO>
      { module.category.toXML } 
    </EXTRA>
  }
}

trait DumpQuestion extends XMLOuput {
  val question: Question

  implicit def answer2DumpAnswer(answer: Answer) = new DumpAnswer(answer)

  // Look at a better way to get the actual question type
  def toXML = {
    <QUESTION>
      <ID>{ question.id }</ID>
      <TYPE>{ question.getClass.getInterfaces.head.getSimpleName }</TYPE>
      <NAME>{ question.name }</NAME>
      <TEXT>{ question.text }</TEXT>
      <GRADE>{ question.grade }</GRADE>
      <ANSWERS>{ question.answers.map(_.toXML) }
      </ANSWERS>
    </QUESTION>
  }
}

class DumpAnswer(val answer: Answer) extends XMLOuput {
  def toXML = {
    <ANSWER>
      <ID>{answer.id}</ID>
      <NAME>{answer.text}</NAME>
      <WEIGHT>{answer.weight}</WEIGHT>
      <FEEDBACK>{answer.feedback}</FEEDBACK>
    </ANSWER>
  }
}

class DumpCategory(val module: QuestionCategory) extends DumpModule {
  val under = CourseModule(module, 0, Nil)

  implicit def question2DumpQuestion(q: Question) = q match {
    case multi: MultipleChoice => new DumpMultipleChoice(multi)
    case essay: Essay => new DumpEssay(essay)
    case tf: BooleanQuestion => new DumpBooleanQuestion(tf)
    case matc: Matching => new DumpMatching(matc)
    case order: Ordering => new DumpOrdering(order)
    case fillb: FillInBlank => new DumpFillInBlank(fillb)
    case num: Numeric => new DumpNumeric(num)
  }

  override def extraXML = {
    <CATEGORY>
      <INFO>{ module.info }</INFO>
      <QUESTIONS>{ module.questions.map(_.toXML) }
      </QUESTIONS>
    </CATEGORY>
  }
}

class DumpMultipleChoice(val question: MultipleChoice) extends DumpQuestion {
  override def extraXML = {
    <EXTRA>
      <INCORRECTFEEDBACK>{ question.incorrectFeedback }</INCORRECTFEEDBACK>
      <CORRECTFEEDBACK>{ question.correctFeedback}</CORRECTFEEDBACK>
    </EXTRA>
  }
}

class DumpMatching(val question: Matching) extends DumpQuestion
class DumpBooleanQuestion(val question: BooleanQuestion) extends DumpQuestion {
  override def extraXML = {
    <EXTRA>
      <TRUE>{ question.trueAnswer }</TRUE>
      <FALSE>{ question.falseAnswer }</FALSE>
    </EXTRA>
  }
}
class DumpEssay(val question: Essay) extends DumpQuestion
class DumpOrdering(val question: Ordering) extends DumpQuestion
class DumpFillInBlank(val question: FillInBlank) extends DumpQuestion
class DumpNumeric(val question: Numeric) extends DumpQuestion {
  override def extraXML = {
    <EXTRA>
      <TOLERANCE>{ question.tolerance }</TOLERANCE>
    </EXTRA>
  }
}
