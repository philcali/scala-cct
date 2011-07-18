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
package knowledge

import scala.xml.{Elem, Node, NodeSeq}
import course._

/**
 * A knowledge knows about whatever we're dealing with, and creates a Course object
 * @author Philip Cali
 */
trait Knowledge {

  val working: String 

  def source: Elem

  def make: Course

  def traverse[A](node: NodeSeq, level: Int = 0): Seq[CourseModule] 

  val nondisplay: Seq[Module]

}
