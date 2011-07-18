// Scala Course Converter is a command-line utility that gives the end user
// the ability to convert one e-learning management system course archive
// to another e-learning management system course archive.
// 
// 
// Copyright (C) 2010 Philip Cali 
// 
// 
// Scala Course Converter is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.github.philcali.cct
package course

class Module(val id: Int, val name: String, val from: String)
trait Resource extends Module

// Online Course Module
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
