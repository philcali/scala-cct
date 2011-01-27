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
