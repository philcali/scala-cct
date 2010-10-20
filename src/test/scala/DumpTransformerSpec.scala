package com.philipcali.cct
package test

import dump.DumpTransformer
import java.io.File
import Zip.extract
import scala.xml.XML

class DumpTransformerSpec extends TransformerSpec {
  val transformer = DumpTransformer(working, "temp")

  def file = XML.load(working + "_dump/dump.xml")

  "Dump Transformer" should "produce a dump archive" in {
    new File(working + "_dump.zip") should be ('exists)
  }

  it should "produce a replicate directory structure" in {
    extract(working + "_dump.zip", "temp")

    new File(working + "_dump/syllabus_dir") should be ('exists)
    new File(working + "_dump/important_dir") should be ('exists)
  }

  it should "produce a valid xml" in {
    (file \\ "COURSE").size should be === 1
  }
  
  it should "produce the right number of sections" in {
    (file \\ "ORGANIZATION" \ "MODULE").size should be === 3
  }

  "The first section" should "produce the correct number of modules" in {
    ((file \\ "ORGANIZATION" \ "MODULE")(0) \ "MODULES" \\ "MODULE") .size should be === 5
  }

  it should "produce the correct module types" in {
    val types = for(module <- (file \\ "ORGANIZATION" \ "MODULE")(0) \ "MODULES" \\ "MODULE") 
      yield {
        module \ "TYPE" text
      }
    
    val expected = List("Label", "SingleFile", "OnlineDocument", "Directory", "ExternalLink")

    types.mkString(" ") should be === expected.mkString(" ")
  }

  "Non display content" should "render just the same" in {
    (file \\ "NONDISPLAY" \ "MODULE").size should be === 1
  }

  it should "contain a question category" in {
    (file \\ "NONDISPLAY" \ "MODULE" \ "TYPE").text should be === "QuestionCategory"
  }

  "Question Category" should "contain the right questions" in {
    val types = for(question <- file \\ "NONDISPLAY" \\ "QUESTION") yield (question \ "TYPE" text)

    val expected = List("MultipleChoice", "Essay", "BooleanQuestion", "Matching")

    types.mkString(" ") should be === expected.mkString(" ")
  }
}
