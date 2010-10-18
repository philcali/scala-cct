package com.philipcali.cct

package dump

import Zip.archive
import system.TransformerTag
import transformer.Transformer
import clean.{remove, copy}
import course._
import java.io.{File => JFile}
import DumpConversions._

class DumpTransformerTag extends TransformerTag {
  def name = "dump"
  def version = "1.0"
  def suffix = "_dump"
}

object DumpTransformer extends DumpTransformerTag {
  def tag = new DumpTransformerTag
  def apply(working: String, output: String = "./") = new DumpTransformer(working, output)
}

class DumpTransformer(val working: String, val output: String) extends Transformer {
  def staging = {
    val newDir = new JFile(working + "_dump")
    
    if(!newDir.exists) {
      newDir.mkdir
    }

    newDir.getName
  }

  def transform(course: Course) {
    def courseXml = {
      course.sections.foreach { section => 
        course.mods(section).foreach(_.transform(working, staging))
      }

      <COURSE>
        <INFO>
          <TITLE>{course.info.title}</TITLE>
          <DESCRIPTION>{course.info.description}</DESCRIPTION>
        </INFO>
        <ORGANIZATION>{ course.sections.map(_.toXML) }
        </ORGANIZATION>
        <NONDISPLAY>
        </NONDISPLAY>
      </COURSE>
    }
  }

  def cleanup() = { 
    remove(staging)
    remove(working)
  }
}

