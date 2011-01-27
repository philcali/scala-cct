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
