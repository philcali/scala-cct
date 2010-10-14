package com.philipcali.cct

package system
import finder._

trait MetaTag {
  def name: String
  def description: String
}

trait TransformerTag extends MetaTag {
  def suffix: String
  def version: String

  def description = "%s transforms a course object into a %s %s" format(name, suffix, version)
}

trait KnowledgeTag extends MetaTag {
  def version: String
  def description = "%s transforms a %s into a course object." format(name, version)
}

trait Embed {
  val embedPattern: scala.util.matching.Regex
  val defaults = "--EmbedLocation--"

  def knowText(str: String) = embedded(str, defaults)
  def embedded(str: String, to: String) = embedPattern.replaceAllIn(str, to)
}

trait Tagger[A] {
  def tag: A
}
