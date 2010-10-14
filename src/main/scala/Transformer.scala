package com.philipcali.cct

package transformer

import course.Course

trait Transformer {
  def staging: String
  def transform(course: Course)
  def cleanup()
}
