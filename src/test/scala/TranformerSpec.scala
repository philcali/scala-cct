package com.philipcali.cct
package test

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import grizzled.util.{withCloseable => withc}
import java.io.{File => JFile, FileWriter}

import transformer._
import course._
import Utils._

trait TransformerSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  val transformer: Transformer

  // Package of the defined transformer
  lazy val  (name, pack) = {
    val split = transformer.getClass.getPackage.getName.split("\\.")
    (split.last, split.dropRight(1).mkString("."))
  }

  val working = "temp/wicket"    

  def course: Course = {
    val category = new QuestionCategory(8, "Test1", "", "This is your final test",
      new Question(9, "Who is your master?", "Who is it?", 5.0) with MultipleChoice {
        override def incorrectFeedback = "Incorrect"
        def answers = {
          Answer(1, "Me", 1.0) ::
          Answer(2, "You", 0.0) ::
          Answer(3, "The ring", 0.0) ::
          Answer(4, "Gimli", 0.0) :: Nil
        }
      } ::
      new Question(10, "Short Answer", "Name your favorite employee", 5.0) with Essay {
      } ::
      new Question(11, "True / False", "Does Blackboard suck?", 5.0) with BooleanQuestion {
        def answers = {
          Answer(1, "True", 1.0) ::
          Answer(2, "False", 0.0) :: Nil
        }
      } ::
      new Question(12, "Matching", "Match the corresponding", 5.0) with Matching {
        def answers = {
          Answer(1, "Schmee", 1.0) ::
          Answer(2, "Robert", 1.0) ::
          Answer(3, "Philip", 1.0) ::
          Answer(4, "Micahael", 1.0) :: Nil
        }
      } :: Nil)

    new Course(CourseHeader("A Test Course", "Test Description"),
      CourseModule(new Section("Section 1"), 0, 
        CourseModule(new Label(1, "Documents", ""), 1, 
          CourseModule(new SingleFile(2, "Syllabus", "syllabus_dir",
            File("syllabus.md", "syllabus.md", 0)), 2, Nil) ::
          CourseModule(new OnlineDocument(3, "Web Page", "", "--EmbedLocation--"), 2, Nil) ::
          CourseModule(new Directory(4, "Important Stuff", "important_dir",
            File("test1.txt", "test1.txt", 0) ::
            File("test2.txt", "test2.txt", 0) ::
            File("test3.txt", "test3.txt", 0) :: Nil), 2, Nil) ::
          CourseModule(new ExternalLink(5, "Blog", "", "http://philcalicode.blogspot.com"), 2, Nil)
            :: Nil) :: Nil) ::
      CourseModule(new Section("Staff Information"), 0,
          CourseModule(new StaffInformation(6, "Pro. Philip Cali", "", 
            Contact("Mr.", "Philip", "Cali", "calico.software@gmail.com", "", "", "")), 1, Nil) 
            :: Nil) ::
      CourseModule(new Section("Assignments"), 0,
        CourseModule(new Quiz(7, "Test1", "", "This is your final exam", category), 1, Nil)
            :: Nil) :: Nil,
      category :: Nil)
  }

  override def beforeAll(config: Map[String, Any]) {
    new JFile(working).mkdirs
    new JFile(working + "/syllabus_dir").mkdir
    new JFile(working + "/important_dir").mkdir
   
    withc(new FileWriter(working + "/syllabus_dir/syllabus.md")) { w =>
      val content = List("# Syllabus", "## What to expect", " * Quizzes", " * Tests", " * Owned")
      w.write(content mkString("\n"))
    }

    for(index <- (1 to 3)) {
      withc(new FileWriter(working + "/important_dir/test" + index + ".txt")) { w =>
        w.write("I have something in this file: " + index)
      }
    }
  }

  "A transformer" should "perform a transform without exception" in {
    transformer.transform(course)
  }

  it should "be discoverable by the MetaFinder" in {
    val expected = finder.MetaFinder.find("Transformer", name, pack)

    transformer.getClass should be === expected
  }

  override def afterAll(config: Map[String, Any]) {
    remove("temp")
  }
}
