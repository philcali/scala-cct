package com.philipcali.cct
package knowledge

import scala.xml.{Elem, Node, NodeSeq}
import course._

trait Knowledge {

  val working: String 

  def source: Elem

  def make: Course

  def traverse[A](node: NodeSeq, level: Int = 0): Seq[CourseModule] 

  val nondisplay: Seq[Module]

  def handleDocument(ref: String, xml: Node): Module
  def handleSection(ref: String): Section
  def handleStaff(ref: String): StaffInformation 
  def handleTest(ref: String, xml: Node): Quiz 
  def handleLabel(ref: String, xml: Node): Label
  def handleUnknown(ref: String): Label
}
