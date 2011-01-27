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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import finder.MetaFinder
import system._

/**
 * @author Philip Cali
 **/
object TestKnowledge extends Tagger[TestKnowledge]{
  def tag = new TestKnowledge
}

object TestTransformer extends Tagger[TestTransformer] {
  def tag = new TestTransformer
}

class TestKnowledge extends KnowledgeTag {
  def name = "test"
  def version = "1.0"
}

class TestTransformer extends TransformerTag {
  def name = "test"
  def suffix = "Test"
  def version = "1.0"
}

class MetaFinderSpec extends FlatSpec with ShouldMatchers {
  "MetaFinder" should "find the right class" in {
    val clazz = MetaFinder.find("Knowledge", "test") 
    clazz should be === classOf[TestKnowledge]
  }

  it should "find test knowledge tag" in { 
    val knowledgeTag = new TestKnowledge    

    MetaFinder.knowledgeTag("test").description should be === knowledgeTag.description
  }

  it should "find test transformer tag" in {
    val transformerTag = new TestTransformer

    MetaFinder.transformerTag("test").description should be === transformerTag.description
  }

  it should "should throw an exception" in {
    evaluating { MetaFinder.knowledgeTag("nothing") } should produce [Exception]
  }
}