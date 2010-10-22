package com.philipcali.cct
package test

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import knowledge._
import system._
import course._
import Utils.remove

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
