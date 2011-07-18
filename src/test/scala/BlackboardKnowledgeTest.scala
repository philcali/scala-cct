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
