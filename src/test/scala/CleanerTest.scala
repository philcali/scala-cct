package com.philipcali.cct
package test

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import clean._
import java.io.{File, FileWriter}
import grizzled.util.{withCloseable => withc}

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

  it should "remove parent directory" in {
    remove("parent")

    new File("parent") should not be ('exists)
  }
}