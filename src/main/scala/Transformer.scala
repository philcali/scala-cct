package com.philipcali.cct

package transformer

import course.Course

/**
 * Entry for all Transformers. 
 * @author Philip Cali
 */
trait Transformer {
  val working: String

  def staging: String

  /**
   * Transform a Course object into something useful
   */
  def transform(course: Course)
  def cleanup()
}
