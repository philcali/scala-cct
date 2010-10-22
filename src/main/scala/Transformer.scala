package com.philipcali.cct

package transformer

import course.Course

/**
 * Entry for all Transformers. 
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
