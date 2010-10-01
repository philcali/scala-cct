package com.philipcali.cct
package course

class Module(id: Int, name: String, from: String)

// Online Course Module
case class Label(id: Int, name: String, from: String) extends Module(id, name, from)
case class ExternalLink(id: Int, name: String, from: String, url: String) extends Module(id, name, from)
case class OnlineDocument(id: Int, name: String, from: String, text: String) extends Module(id, name, from)
case class SingleFile(id: Int, name: String, from: String, file: File) extends Module(id, name, from)
case class Directory(id: Int, name: String, from: String, directory: List[File]) extends Module(id, name, from)
case class File(name: String, linkname: String, size: Long)

// Discussion / Anouncement
case class Announcement(id: Int, name: String, from: String, shorttext: String) extends Module(id, name, from)

// Forums
case class Forum(id: Int, name: String, from: String, shorttext: String) extends Module(id, name, from)

// Staff Information
case class StaffInformation(id: Int, name: String, from: String, contact: Contact) extends Module(id, name, from)
case class Contact(title: String, given: String, family: String, 
                   email: String, phone: String, office: String, image: String)


// Quiz / Assignments
case class Quiz(id: Int, name: String, from: String, description: String) extends Module(id, name, from)

// Question / Question types
case class QuestionCategory(id: Int, name: String, from: String, info: String, questions: Seq[Question]) extends Module(id, name, from)
abstract case class Question(id: Int, name: String, from: String,  
                             text: String, grade: Double) extends Module(id, name, from) {
  def answers: Seq[Answer]
}

trait MultipleChoice extends Question {
  def correctFeedback = ""
  def incorrectFeedback = ""
}

trait Essay extends Question {
  def answers = List(Answer(1))
}

trait BooleanQuestion extends Question {
  def trueAnswer = 0
  def falseAnswer = 1
}

trait Matching extends Question
trait Ordering extends Question
trait FillInBlank extends Question
trait Numeric extends Question

case class Answer(id: Int, text: String="", weight: Double=0.0, feedback: String = "")

case class Section(name: String) extends Module(0, name, "")
case class CourseHeader(title: String, description: String)
case class CourseModule(wrapped: Module, level: Int, children: Seq[CourseModule])

// The main Course
class Course(val info: CourseHeader, val sections: Seq[CourseModule], val nondisplay: Seq[Module]) {
  def printStructure {
    def printModule(d: Seq[CourseModule]) {
      d foreach { o =>
        val tab = (0 until o.level).foldLeft("")((in, str) => in + "\t")
        println(tab + o.wrapped); printModule(o.children)
      }
    }
    println("Title: " + info.title)
    println("Description: " + info.description)
    println("Modules in organization:")
    printModule(sections)
    println("Modules not in organization (Announcements/Question Categories):")
    nondisplay.foreach(println)
  }
}
