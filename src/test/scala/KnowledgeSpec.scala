package com.philipcali.cct
package test

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import knowledge._
import system._
import course._

trait KnowledgeSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  val knowledge: Knowledge
}
