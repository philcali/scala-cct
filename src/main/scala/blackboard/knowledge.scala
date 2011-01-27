package com.philipcali.cct
package blackboard

import system.{KnowledgeTag, Tagger, Embed}
import knowledge._
import course._
import Utils._
import scala.xml._
import Zip.extract

/**
 * @author Philip Cali
 **/
class BlackboardKnowledgeTag extends KnowledgeTag {
  def name = "blackboard"
  def version = "6.5+"
}

/**
 * @author Philip Cali
 **/
object BlackboardKnowledge extends Tagger[BlackboardKnowledgeTag] {
  def tag = new BlackboardKnowledgeTag
  def apply(archive: String) = new BlackboardKnowledge(archive)
}

/**
 * @author Philip Cali
 **/
class BlackboardKnowledge(val archive: String) extends Knowledge with Embed {
  val embedPattern = """@X@EmbeddedFile\.location@X@""".r  

  val working = "temp/" + archive.split("/").last.split("\\.")(0)

  // The manifest source
  def source = XML.loadFile(working + "/imsmanifest.xml")

  // Namespace
  def bb = source.getNamespace("bb")

  // Creates a map with the resource identifier and the resource
  lazy val resources = for(node <- source \\ "resource") yield { 
     Resource(node.attribute(bb, "title").get.text, 
              node.attribute("identifier").get.text,
              node.attribute("type").get.text)
  }

  // Creates a object tree which will need to be parsed
  def traverse[A](node: NodeSeq, level: Int = 0): Seq[CourseModule] = {
    for(n <- node \ "item"; 
      val name = n \ "title" text; 
      val ref = n.attribute("identifierref").get.text) 
    yield {
      val handler = defineResource(ref)
      CourseModule(handler(ref), level, traverse(n, level + 1))
    }
  }

  def withResources[A](filter: Resource => Boolean)(handle: (Resource, Node) => A) = {
    resources.filter(filter).map { r=>
      withDat(r.ident) { file =>
        handle(r, file)
      }
    }
  }

  lazy val nondisplay = announcements ++ categories

  def announcements = {
    withResources(_.tpe contains("announcement")) { (res, file) =>
      withIdName(file) { (id, name) => 
        new Announcement(id, name, res.ident, file \\ "TEXT" text)
      }
    } 
  }

  def categories = {
    withResources(res => res.tpe.contains("qti") && !res.tpe.endsWith("attempt")) { (res, file) => 
      val id = (file \\ "assessment" \ "assessmentmetadata" \ "bbmd_asi_object_id" text).split("_")(1)
      val info = (file \\ "assessment" \ "presentation_material" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text)

      new QuestionCategory(id.toInt, res.title, res.ident, info, questions(file))
    }
  }

  def questions[A >: Question](file: Node): Seq[A] = {
    for(question <- file \\ "assessment" \ "section" \ "item") yield {

      val builder = questionBuilder(question) _

      question \\ "bbmd_questiontype" text match {
        case "Multiple Choice" => builder(handleMultipleChoice) 
        case "Multiple Answer" => builder(handleMultipleChoice)
        case "Opinion Scale" => builder(handleMultipleChoice)
        case "Essay" => builder(handleEssay) 
        case "Short Response" => builder(handleEssay)
        case "True/False" => builder(handleBoolean)
        case "Either/Or" => builder(handleBoolean)
        case "Matching" => builder(handleMatching)
        case "Ordering" => builder(handleOrdering)
        case "Fill in the Blank" => builder(handleFillInBlank)
        case "Numeric" => builder(handleNumeric)
        case _ => builder(handleEssay)
      }
    }
  }

  def questionBuilder[A >: Question](question: NodeSeq)(handler: (NodeSeq, Int, String, Double) => A)= {
    val id = (question \\ "bbmd_asi_object_id" text).split("_")(1)
    val name = (question \ "presentation" \ "flow" \ "flow" \ "flow" \ "material" \ "mat_extension" \ "mat_formattedtext" text)
    val grade = (question \ "itemmetadata" \ "qmd_absolutescore_max" text).toDouble
  
    handler(question, id.toInt, name, if(grade == 0) 1 else grade)
  }

  def feedback(question: NodeSeq, index: Int) = (question \ "itemfeedback")(index) \ "flow_mat" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text

  def value(question: NodeSeq, of: String) = (question \ "resprocessing" \ "outcomes" \ "decvar")(0) \ of text
  
  def handleMultipleChoice(question: NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with MultipleChoice {
      override def correctFeedback = feedback(question, 0)
      override def incorrectFeedback = feedback(question, 1) 
      def answers  =  {
        for((answer, index) <- (question \ "presentation" \ "flow" \ "flow")(1) \ "response_lid" \ "render_choice" \ "flow_label" zipWithIndex)
        yield {
          val text = answer \ "response_label" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text
          val answercode = (answer \ "response_label")(0) \ "@ident" text
          val weight = (question \ "resprocessing" \ "respcondition" \ "conditionvar").find(_.text == answercode) match {
            case Some(node) => value(question, "@maxvalue")
            case None => value(question, "@minvalue")
          }
          Answer(index + 1, text, if(weight == "") 0 else weight.toDouble)
        }
      }
    }
  }

  def handleEssay(question: NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with Essay
  }

  def handleBoolean(question: NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with BooleanQuestion {
      def answers = {
        def defineAnswer(desc: String) = {
          ((question \ "resprocessing" \ "respcondition")(0) 
            \ "conditionvar" \ "varequal").find(_.text.equalsIgnoreCase(desc)) match {
            case Some(node) => Answer(0, desc, 1, feedback(question, 0))
            case None => Answer(0, desc, 0, feedback(question, 1))
          }
        }
        List("True", "False") map(defineAnswer) 
      }
    }
  }

  def handleMatching(question: NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with Matching {
      def answers = {
        for((answer, index) <- (question \ "presentation" \ "flow" \ "flow")(1) \ "flow" zipWithIndex) 
        yield {
          val text = (answer \ "material" \ "mat_extension" \ "mat_formattedtext" text)
          val anstext = (((question \ "presentation" \ "flow")(2) \ "flow")(index+1) \ "flow" \ "material" \ "mat_extension" \ "mat_formattedtext" text)
          Answer(index + 1, text, 1, anstext)
        }
      }
    }
  }

  def handleOrdering(question: NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with Ordering {
      def answers = {
        for((answer, index) <- (question \ "presentation" \ "flow" \ "flow" \ "response_lid" \"render_choice" \ "flow_label" zipWithIndex))
        yield {
          val anstext = (answer \ "response_label" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text)
          Answer(index, (index + 1).toString, 1, anstext)
        }
      }
    }
  }

  def handleFillInBlank(question: NodeSeq, id: Int, name: String, grade: Double) = {
    val ans = question \ "resprocessing" \ "respcondition" \ "conditionvar" \ "varequal"

    if(ans.size > 0) {
      new Question(id, name, name, grade) with FillInBlank {
        def answers = {
          for((answer, index) <- ans.zipWithIndex)
          yield {
            Answer(index, answer text, 1, "Correct")
          }
        }
      }
    } else {
      handleEssay(question, id, name, grade)
    }
  }

  def handleNumeric(question : NodeSeq, id: Int, name: String, grade: Double) = {
    new Question(id, name, name, grade) with Numeric {
      override def tolerance = {
        val varite = (question \ "resprocessing" \ "respcondition" \ "conditionvar" \ "varite" text)
        val varequal = (question \ "resprocessing" \ "respcondition" \ "conditionvar" \ "varequal" text)
        (varite.toDouble - varequal.toDouble)
      }
  
      def answers = {
        val text = (question \ "resprocessing" \ "respcondition" \ "conditionvar" \ "varequal" text)
        val feed = (question \ "itemfeedback" \\ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text)
        List(Answer(1, text, 1, feed))
      }
    }
  }

  def make = {
    // Extract our zip
    extract(archive, "temp")
    
    val header = parseInfo
    new Course(header, traverse(source \\ "organization"), nondisplay)
  }

  def defineResource(ref: String) = {
    resources find(_.ident == ref) match {
      case Some(resource) => {
        resource.tpe match {
          case "course/x-bb-coursetoc" => handleSection _
          case "resource/x-bb-staffinfo" => handleStaff _
          case "resource/x-bb-document" => handleBBDocument _
          case _ => handleUnknown _
        }
      }
      case None => throw new IllegalArgumentException("No matching Resource: " + ref)
    }
  }

  def withDat[A](ref: String)(block: Node => A) = {
    val file = XML.loadFile(working + "/" + ref + ".dat")
    block(file)
  }

  /**
   * Common enough to require it
   */
  def withIdName[A](xml: Node)(fun: (Int, String) => A) = {
    val id = xml.attribute("id").get.text.split("_")(1)
    val name = (xml \\ "TITLE")(0) \ "@value" text
    
    fun(id.toInt, name)
  }

  def handleTest(ref: String, file: Node) = {
    withIdName(file) { (id, name) => 
      new Quiz(id, name, ref, file \\ "TEXT" text, 
        nondisplay.find(m => m.name == name && 
                        m.isInstanceOf[QuestionCategory]).get.asInstanceOf[QuestionCategory])
    }
  }

  def handleSection(ref: String) = {
    withDat(ref) { file =>
      val label = (file \\ "LABEL")(0) \ "@value" text
      val name = label.split("\\.")(1) match {
        case "StaffInformation" => "Staff Information"
        case "CourseDocuments" => "Course Documents"
        case "ExternalLinks" => "External Links"
        case "CourseInformation" => "Course Information"
        case "DiscussionBoard" => "Discussion Board"
        case someString: String => someString
      }
      new Section(name)
    }
  }

  def handleStaff(ref: String) = {
    withDat(ref) { staffinfo =>
      val id = staffinfo.attribute("id").get.text.split("_")(1)
    
      // Internal function to parse out correct info
      def nameparse(node: String) = (staffinfo \\ "CONTACT" \\ node)(0) \ "@value" text
      val formaltitle = nameparse("FORMALTITLE")
      val given = nameparse("GIVEN")
      val family = nameparse("FAMILY")
      val email = nameparse("EMAIL")
      val phone = nameparse("PHONE")
      val office = nameparse("ADDRESS")
      val image = (staffinfo \\ "IMAGE")(0) \ "@value" text

      val contact = Contact(formaltitle, given, family, email, phone, office, image)  
      new StaffInformation(id.toInt, "%s %s %s" format(formaltitle, given, family), ref, contact) 
    }
  }

  def handleBBDocument(ref: String) = {
    withDat(ref) { file => 
      (file \ "CONTENTHANDLER")(0) \ "@value" text match {
        case "resource/x-bb-folder" => handleLabel(ref, file)
        case "resource/x-bb-document" => handleDocument(ref, file)
        case "resource/x-bb-externallink" => handleDocument(ref, file)
        case "resource/x-bb-asmt-test-link" => handleTest(ref, file)
        case "resource/x-bb-asmt-survey-link" => handleTest(ref, file)
        case _ => handleLabel(ref, file)
      }
    }
  }

  def handleDocument(ref: String, xml: Node) = {
    withIdName(xml) { (id, name) =>
      val files = (xml \\ "FILES") \ "FILE"
      val text = (xml \\ "TEXT" text)     
      val url = (xml \\ "URL")(0) \ "@value" text
 
      // Handling of the particulars 
      if(url != "") {
        new ExternalLink(id, name, ref, url)
      } else if(files.size > 1) {
        new Directory(id, name, ref, processFiles(ref, files))
      } else if(files.size == 1) {
        new SingleFile(id, name, ref, processFiles(ref, files).head)
      } else if(text != ""){
        new OnlineDocument(id, name, ref, knowText(text))
      } else {
        new Label(id, name, ref)
      }
    }
  }

  def processFiles(ref: String, files: NodeSeq) = {
    (for(file <- files; 
      val name = (file \ "NAME" text);
      val linkname = (file \ "LINKNAME")(0) \ "@value" text;
      val size = ((file \\ "SIZE")(0) \ "@value" text).toLong) 
    yield {
      val dir = new java.io.File(working + "/" + ref)
      dir.listFiles.find(_.getName == linkname) match {
        case Some(f) => File(name, linkname, size)
        case None => {
          val newName = fileclean(name)
          val badFile = dir.listFiles.find(_.length == size).get
          val newFile = new java.io.File(working + "/" + ref + "/" + newName)
          badFile.renameTo(newFile)
          File(newName, newName, size)
        }
      }
    }).toList
  }

  def handleLabel(ref: String, xml: Node) = {
    withIdName(xml) { (id, name) =>
      new Label(id, name, ref)
    }
  }

  /**
   * Handling the unknown cases will transorm the element into a Label
   */
  def handleUnknown(ref: String) = {
    withDat(ref) { file =>
      withIdName(file) { (id, name) => new Label(id, name, ref) }
    }
  }

  def parseInfo = {
    resources.find(_.tpe contains("coursesetting")) match {
      case Some(resource) => withDat(resource.ident) { courseinfo => 
        new CourseHeader((courseinfo \ "TITLE").head.attribute("value").get.text,
                         (courseinfo \ "DESCRIPTION").head.text)
      }
      case None => throw new IllegalArgumentException("Invalid Course Header")
    }
  }

  def pretty(ref: String) {
    withDat(ref) { file =>
      def nodePrint(node: Node, indent: String = "") {
        println(indent + node.label + " " + node.attributes + " " + node.text)
        for(n <- node.child) { 
          nodePrint(n, indent + "\t")
        }
      }
      nodePrint(file)
    }
  }
}

// Middle conversion; A Resource may be BB specific
/**
 * @author Philip Cali
 **/
case class Resource(title: String, ident: String, tpe: String)
