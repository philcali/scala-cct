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
