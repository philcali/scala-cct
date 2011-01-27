package com.philipcali.cct
package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import system.Embed

/**
 * @author Philip Cali
 **/
class EmbedSpec extends FlatSpec with ShouldMatchers {
  val preformatted = "There should be a %ssomething here and %ssomething here"
 
  "Embedded classes" should "replace specified pattern" in {
    val embed = "@X@EmbeddedFile.Location@X@"
    val text = preformatted format (embed, embed)
    
    val translater = new Embed {
      val embedPattern = embed.r
    }

    val postFormatted = preformatted format(translater.defaults, translater.defaults)

    translater.knowText(text) should be === postFormatted
  }

  it should "replace the defaults" in {
    val embed = """\$\$FILESHERE\$\$"""
    val em = "$$FILESHERE$$"  
 
    val translater = new Embed {
      val embedPattern = defaults.r 
    }

    val text = preformatted format (translater.defaults, translater.defaults)
    val postFormatted = preformatted format (em, em)

    translater.embedded(text, embed) should be === postFormatted
  }
}
