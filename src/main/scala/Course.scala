package com.philipcali.cct
package course

class Module(val id: Int, val name: String, val from: String)
trait Resource extends Module

// Online Cou`rse Module
class Label(id: Int, name: String, from: String) extends Module(id, name, from)
class ExternalLink(id: Int, name: String, from: String, val url: String) extends Module(id, name, from) with Resource
class OnlineDocument(id: Int, name: String, from: String, val text: String) extends Module(id, name, from) with Resource
class SingleFile(id: Int, name: String, from: String, val file: File) extends Module(id, name, from) with Resource
class Directory(id: Int, name: String, from: String, val directory: List[File]) extends Module(id, name, from) with Resource
case class File(name: String, linkname: String, size: Long)

// Discussion / Anouncement
class Announcement(id: Int, name: String, from: String, val shorttext: String) extends Module(id, name, from)

// Forums
class Forum(id: Int, name: String, from: String, val shorttext: String) extends Module(id, name, from)

// Staff Information
class StaffInformation(id: Int, name: String, from: String, val contact: Contact) extends Module(id, name, from) with Resource
case class Contact(title: String, given: String, family: String, 
                   email: String, phone: String, office: String, image: String)


// Quiz / Assignments
class Quiz(id: Int, name: String, from: String, val description: String, val category: QuestionCategory) extends Module(id, name, from)

// Question / Question types
class QuestionCategory(id: Int, name: String, from: String, val info: String, val questions: Seq[Question]) extends Module(id, name, from)
abstract class Question(id: Int, name: String,  
                        val text: String, val grade: Double) extends Module(id, name, "") {
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
trait Numeric extends Question {
  def tolerance = 0.0
}

case class Answer(id: Int, text: String="", weight: Double=0.0, feedback: String = "")

class Section(name: String) extends Module(0, name, "")
case class CourseHeader(title: String, description: String)
case class CourseModule(wrapped: Module, level: Int, children: Seq[CourseModule])

// The main Course
class Course(val info: CourseHeader, val sections: Seq[CourseModule], val nondisplay: Seq[Module]) {
  def traverseModule[A](d: Seq[CourseModule])(ret: CourseModule => A): Seq[A] = {
    d flatMap { o =>
      Seq(ret(o)) ++ traverseModule(o.children)(ret)
    }
  }

  def mods(section: CourseModule) = {
    traverseModule(section.children)(m => m)
  } 
 
  def details = {
    traverseModule(sections)(m => m.wrapped)
  }

  def printStructure {
    def printModule(d: Seq[CourseModule]) {
      d foreach { o =>
        val tab = (0 until o.level).foldLeft("")((in, str) => in + "\t")
        println(tab + o.wrapped.name + " " + o.wrapped.from); printModule(o.children)
      }
    }
    println("Title: " + info.title)
    println("Description: " + info.description)
    println("Modules in organization:")
    printModule(sections)
    println("Modules not in organization (Announcements/Question Categories):")
    nondisplay.foreach(o => println(o.name + " " + o.from))
  }
}
