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
package system
import finder._


/**
 * A tag recognized by the MetaFinder
 * @author Philip Cali
 */
trait MetaTag {
  def name: String
  def description: String
  def conversion: String
}

/**
 * A Transformer tag recognized by the MetaFinder
 * @author Philip Cali
 */
trait TransformerTag extends MetaTag {
  def suffix: String
  def version: String
  def conversion = "Transformer"

  def description = "%s transforms a course object into a %s %s" format(name, suffix, version)
}

/**
 * A Knowledge tag recognized by the MetaFinder
 * @author Philip Cali
 */
trait KnowledgeTag extends MetaTag {
  def version: String
  def conversion = "Knowledge"
  def description = "%s transforms a %s into a course object." format(name, version)
}

/**
 * Used to find courses with Embedded Location keywords
 * @author Philip Cali
 */
trait Embed {
  val embedPattern: scala.util.matching.Regex
  val defaults = "--EmbedLocation--"

  def knowText(str: String) = embedded(str, defaults)
  def embedded(str: String, to: String) = embedPattern.replaceAllIn(str, to)
}

/**
 * Tag producing objects should inherit this trait
 * @author Philip Cali
 */
trait Tagger[A] {
  def tag: A
}