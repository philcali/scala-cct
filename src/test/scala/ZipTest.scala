package com.philipcali.cct
package test

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers
import Zip._
import Utils.remove

class ZipSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  import java.io.File
  val archivePath = getClass.getClassLoader.getResource("archive.zip")

  override def afterAll(configMap: Map[String, Any]) {
    // Delete temp files
    remove("archive")
    remove("temp")
    remove("../archive.zip")
    remove("archive.zip")
  }

  "Test archive" should "exists" in {
    val archive = new File(archivePath.getFile)
    archive should not be (null)
  }  
  
  "Extract" should """create directory tree: 
  archive/ 
  archive/child/ 
  archive/child/more.txt 
  archive/test.xml""" in {
    extract(archivePath.getFile)
    
    // Checking Dir tree
    new File("archive") should be ('exists)
    new File("archive/test.xml") should be ('exists)
    new File("archive/child") should be ('exists)
    new File("archive/child/more.txt") should be ('exists)
  }

  "Extract into a new directory" should """create directory tree:
  temp/archive
  temp/archive/child/
  temp/archive/child/more.txt
  temp/archive/test.xml""" in {
    extract(archivePath.getFile, "temp")

    new File("temp/archive") should be ('exists)
    new File("temp/archive/test.xml") should be ('exists)
    new File("temp/archive/child") should be ('exists)
    new File("temp/archive/child/more.txt") should be ('exists)
  }

  "Extracted flat files" should "contain correct data" in {
    import scala.xml._
    import scala.io.Source.{fromFile => open}

    val xmltext = <stuff>
  <more-stuff>Test</more-stuff>
</stuff>
    val text = "You'll never find me!"
    
    XML.loadFile("archive/test.xml") should be === xmltext
    open("archive/child/more.txt").getLines.mkString should be === text
  }

  "Archive" should "create archive.zip" in {
    archive("archive")

    new File("archive.zip") should be ('exists)
  }

  it should "create archive.zip in parent directory" in {
    archive("archive", "../")

    new File("../archive.zip") should be ('exists)
  }

  "../archive.zip and archive.zip" should "be the same size" in {
    val parentzip = new File("../archive.zip")
    val archivezip = new File("archive.zip")
    
    parentzip.length should be === archivezip.length
  }

  "Extracting archive.zip" should "not throw any exceptions" in {
    extract("archive.zip")
  }
}
