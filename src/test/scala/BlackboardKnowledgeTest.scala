package com.philipcali.cct
package test

import blackboard._
import course._
import clean._

import finder.MetaFinder.{knowledge => know}

class BlackboardKnowledgeSpec extends KnowledgeSpec {
  val archive = getClass.getClassLoader.getResource("ArchiveFile_test_blackboard.zip") 

  val knowledge = BlackboardKnowledge(archive.getFile)

  override def afterAll(config: Map[String, Any]) {
    remove("temp")
  }

  "Test archive" should "exists" in {
    archive should not be (null)
  }

  "Blackboard Knowledge" should "produce a valid course" in {
    val course = knowledge.make

    val modules = course.details

    course.info.title should be === "Test Blackboard 6.5+ Archive"
    modules.size should be === 27
    modules.filter(m => m.isInstanceOf[SingleFile]).size should be === 9
    assert(modules.find(m => m.isInstanceOf[OnlineDocument]).get.asInstanceOf[OnlineDocument].
           text.contains("--EmbedLocation--"))
    course.nondisplay.size should be === 41
    course.nondisplay.filter(n => n.isInstanceOf[QuestionCategory]).size should be === 40
  }

  it should "be found by the MetaFinder" in {    
    knowledge.getClass should be === know("blackboard", 
      "com.philipcali.cct", archive.getFile).getClass
  }

  it should "produce the same course object as the MetaFinder knowledge" in {
    val course = knowledge.make
    val expected = know("blackboard", "com.philipcali.cct", archive.getFile).make
    
    course.info.title should be === expected.info.title
    course.details.size should be === expected.details.size
  }
}
