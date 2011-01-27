package com.philipcali.cct
package test

import blackboard._
import course._

import finder.MetaFinder.{knowledge => know}

/**
 * @author Philip Cali
 **/
class BlackboardKnowledgeSpec extends KnowledgeSpec {
  val archive = getClass.getClassLoader.getResource("ArchiveFile_test_blackboard.zip") 

  val knowledge = BlackboardKnowledge(archive.getFile)

  def validHeader(course: Course) {
    course.info.title should be === "Test Blackboard 6.5+ Archive"
  }

  def validModules(modules: Seq[Module]) {
    modules.size should be === 27
    modules.filter(m => m.isInstanceOf[SingleFile]).size should be === 9
    assert(modules.find(m => m.isInstanceOf[OnlineDocument]).get.asInstanceOf[OnlineDocument].
           text.contains("--EmbedLocation--"))
  }

  def validNondisplay(nondisplay: Seq[Module]) {
    nondisplay.size should be === 41
    nondisplay.filter(n => n.isInstanceOf[QuestionCategory]).size should be === 40
  }

  it should "be found by the MetaFinder" in {    
    knowledge.getClass should be === know(name, packs, archive.getFile).getClass
  }

  it should "produce the same course object as the MetaFinder knowledge" in {
    val course = knowledge.make
    val expected = know(name, packs, archive.getFile).make
    
    course.info.title should be === expected.info.title
    course.details.size should be === expected.details.size
  }
}
