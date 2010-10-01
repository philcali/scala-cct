package com.philipcali.cct

import course._
import scala.xml._

class IMSManifest(val working: String) {
  // The manifest source
  def source = XML.loadFile(working + "/imsmanifest.xml")

  // Namespace
  val bb = source.getNamespace("bb")

  // Creates a map with the resource identifier and the resource
  val resources = for(node <- source \\ "resource") yield { 
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

  def withResources[A](filter: String)(handle: (Resource, Node) => A) = {
    resources.filter(_.tpe.contains(filter)).map { r=>
      withDat(r.ident) { file =>
        handle(r, file)
      }
    }
  }

  def announcements = {
    withResources("announcement") { (res, file) =>
      withIdName(file) { (id, name) => 
        Announcement(id, name, res.ident, file \\ "TEXT" text)
      }
    } 
  }

  def categories = {
    withResources("qti") { (res, file) => 
      val id = (file \\ "assessment" \ "assessmentmetadata" \ "bbmd_asi_object_id" text).split("_")(1)
      val info = (file \\ "assessment" \ "presentation_material" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text)

      QuestionCategory(id.toInt, res.title, res.ident, info, questions(file))
    }
  }

  def questions[A >: Question](file: Node): Seq[A] = {
    for(question <- file \\ "assessment" \ "section" \ "item") yield {
      val id = (question \\ "bbmd_asi_object_id" text).split("_")(1)
      val name = (question \ "presentation" \ "flow" \ "flow" \ "flow" \ "material" \ "mat_extension" \ "mat_formattedtext" text)

      question \\ "bbmd_questiontype" text match {
        case "Multiple Choice" => handleMultipleChoice(question, id.toInt, name) 
        case "Multiple Answer" => handleMultipleChoice(question, id.toInt, name)
        case "Opinion Scale" => handleMultipleChoice(question, id.toInt, name)
      }
    }
  }

  def feedback(question: NodeSeq, index: Int) = (question \ "itemfeedback")(index) \ "flow_mat" \ "flow_mat" \ "material" \ "mat_extension" \ "mat_formattedtext" text

  def value(question: NodeSeq, of: String) = (question \ "resprocessing" \ "outcomes" \ "decvar")(0) \ of text

  def grade(question: NodeSeq) = (question \ "itemmetadata" \ "qmd_absolutescore_max" text).toDouble
  
  def handleMultipleChoice(question: NodeSeq, id: Int, name: String) = {
    new Question(id, name, "", name, 
                 if(grade(question) == 0) 1 else grade(question)) with MultipleChoice {
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
          Answer(index, text, weight.toDouble)
        }
      }
    }
  }

  def make = {
    val header = parseInfo
    new Course(header, traverse(source \\ "organization"), announcements)
  }

  def defineResource(ref: String) = {
    resources find(_.ident == ref) match {
      case Some(resource) => {
        resource.tpe match {
          case "course/x-bb-coursetoc" => handleToc _
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
      Quiz(id, name, ref, file \\ "TEXT" text)
    }
  }

  def handleToc(ref: String) = {
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
      Section(name)
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
      StaffInformation(id.toInt, "%s %s %s" format(formaltitle, given, family), ref, contact) 
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
        ExternalLink(id, name, ref, url)
      } else if(files.size > 1) {
        Directory(id, name, ref, processFiles(files))
      } else if(files.size == 1) {
        SingleFile(id, name, ref, processFiles(files).head)
      } else if(text != ""){
        OnlineDocument(id, name, ref, text)
      } else {
        Label(id, name, ref)
      }
    }
  }

  def processFiles(files: NodeSeq) = {
    (for(file <- files; 
      val name = (file \ "NAME" text);
      val linkname = (file \ "LINKNAME")(0) \ "@value" text;
      val size = (file \\ "SIZE")(0) \ "@value" text) 
    yield (File(name, linkname, size.toLong))).toList
  }

  def handleLabel(ref: String, xml: Node) = {
    withIdName(xml) { (id, name) =>
      Label(id, name, ref)
    }
  }

  /**
   * Handling the unknown cases will transorm the element into a Label
   */
  def handleUnknown(ref: String) = {
    withDat(ref) { file =>
      withIdName(file) { (id, name) => Label(id, name, ref) }
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

object DatHandler {
  
}

// Middle conversion; A Resource may be BB specific
case class Resource(title: String, ident: String, tpe: String)
case class Organization(name: String, ref: String, level: Int, children: Seq[Organization])
