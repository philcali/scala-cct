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
/*
import scala.io.Source.{fromFile => open}
import java.io.File
import com.philipcali.cct.Utils.recurse
import java.io.FileWriter

// Run this script with the cct.jar in your classpath
object Lisence {
  val text = open("LICENSE.md").getLines.map("// " + _).mkString("\n")

  def apply(input: File) {
    val source = open(input).getLines.mkString("\n")

    // If the source contains copyright, ignore it.
    if(!source.contains(text)) {
      val writer = new FileWriter(input)
      writer.write(text + "\n" + source)
      writer.close()
    }
  }
}

// Apply Copyright to any file that doesn't have it
recurse(new File(".")) { f => 
  if(f.getName.endsWith(".scala")) Lisence(f) 
}
*/
