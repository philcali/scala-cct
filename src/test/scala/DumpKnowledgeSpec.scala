package com.philipcali.cct
package test

import dump.DumpKnowledge
import course._

class DumpKnowledgeSpec extends KnowledgeSpec {
  val archive = getClass.getResource("Archive_dump.zip")

  val knowledge = DumpKnowledge(archive.getFile)

  def validHeader(course: Course) = {
    course.info.title should be === "Test Course"
  }

  def validModules(course: Course) = {
    course.details.size should be === 7
  }
  
  def validNondisplay(course: Course) = {
    course.nondisplay.size should be === 1
  }
}

