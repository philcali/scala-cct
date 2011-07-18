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

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import Utils._
import java.io.{File, FileWriter}
import grizzled.util.{withCloseable => withc}

/**
 * @author Philip Cali
 **/
class CleanerSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  /**
   * Before this spec runs it needs to build a directory tree
   */
  override def beforeAll(configMap: Map[String, Any]) {
    val parent = new File("parent")
    parent.mkdir
    
    (1 to 2).foreach { index => 
      val child = new File("parent/child" + index)
      child.mkdir
      
      withc(new FileWriter("parent/child" + index + "/test.txt")) { w =>
        w.write("I have something awesome")
      }
    }
  }  

  "Text cleaner" should "clean text" in {
    prune("some wrong string!") should be === "some wrong string"
    prune("Philip's new car?") should be === "Philips new car"
    prune("You can / me @ the club!") should be === "You can  me  the club"
  }

  it should "clean filenames and directory names" in {
    fileclean("How to build something.ppt") should be === "how_to_build_something.ppt"
    fileclean("Philip's new 'car'.docx") should be === "philips_new_car.docx"
    dirclean("Root dir") should be === "root_dir"
  }

  it should "treat fileclean(str) as the same as prune(lower(str), \"\\\\.\")" in {
    val str= "Philip's new 'car'.docx"
    fileclean(str) should be === prune(lower(str), "\\.")
  }

  "Recurse method" should "traverse directory tree" in {
    var count = 0
    recurse(new File("parent")) { f =>
      if(f.isFile) count += 1
    }

    count should be === 2
  }

  it should "copy file contents" in {
    copyFile("parent/child1/test.txt", "parent/child1/test2.txt")
    
    import scala.io.Source.{fromFile => open}

    new File("parent/child1/test2.txt") should be ('exists)
    open("parent/child1/test2.txt").getLines.mkString should be === "I have something awesome"
  }

  it should "copy the directorty contents" in {
    copy(new File("parent"), new File("newParent"))

    new File("newParent/test.txt") should be ('exists)
    new File("newParent/test2.txt") should be ('exists)
  }

  it should "remove parent directory" in {
    remove("parent")

    new File("parent") should not be ('exists)
  }

  it should "remove newParent directory" in {
    remove("newParent")
    
    new File("newParent") should not be ('exists)
  }
}
