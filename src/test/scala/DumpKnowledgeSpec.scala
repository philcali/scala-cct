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
package com.philipcali.cct
package test

import dump.DumpKnowledge
import course._

/**
 * @author Philip Cali
 **/
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
