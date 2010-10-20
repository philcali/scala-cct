package com.philipcali.cct
package test

import dump.DumpKnowledge
import course._

class DumpKnowledgeSpec extends KnowledgeSpec {
  val archive = getClass.getClassLoader.getResource("Archive_dump.zip")

  val knowledge = DumpKnowledge(archive.getFile)

  def validHeader(course: Course) = {
    course.info.title should be === "A Test Course"
    course.info.description should be === "Test Description"
  }

  def validModules(modules: Seq[Module]) = {
    modules.filter(_.isInstanceOf[Section]).size should be === 3
    modules.filter(_.isInstanceOf[Label]).size should be === 1
    modules.filter(_.isInstanceOf[SingleFile]).size should be === 1
    modules.filter(_.isInstanceOf[StaffInformation]).size should be === 1
  }
  
  def validNondisplay(nondisplay: Seq[Module]) = {
    nondisplay.size should be === 1
    nondisplay(0).asInstanceOf[QuestionCategory].questions.size should be === 4
  }
}

