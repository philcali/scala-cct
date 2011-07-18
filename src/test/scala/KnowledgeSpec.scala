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

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import knowledge._
import system._
import course._
import Utils.remove

/**
 * @author Philip Cali
 **/
trait KnowledgeSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  val knowledge: Knowledge

  lazy val (name, packs) = {
    val split = knowledge.getClass.getPackage.getName.split("\\.")
    (split.last, split.dropRight(1).mkString("."))
  }

  override def afterAll(config: Map[String, Any]) {
    remove("temp")
  }

  def validHeader(course: Course)
  def validModules(modules: Seq[Module])
  def validNondisplay(non: Seq[Module])

  "A knowledge" should "produce a course" in {
    val course = knowledge.make
 
    validHeader(course)
    validModules(course.details)
    validNondisplay(course.nondisplay)
  }

}
