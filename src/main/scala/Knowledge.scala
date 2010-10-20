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

}
