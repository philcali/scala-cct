package com.philipcali.cct
package test

import moodle.MoodleTransformer
import moodle.MoodleConversions._

import course.Module
import java.io.File
import Zip.extract

/**
 * @author Philip Cali
 **/
class MoodleTransformerSpec extends TransformerSpec {
  val transformer = MoodleTransformer(working, "temp")

  def findByName(name: String) = findModule(_.name == name)
  def findModule(cond: Module => Boolean) = course.details.find(cond).get

  "Moodle Transformer" should "produce a Moodle archive" in {
    new File(working + "_MoodleImport.zip") should be ('exists)
  }

  it should "have produced a valid moodle.xml" in {
    import scala.xml.XML

    extract(working + "_MoodleImport.zip", "temp")
    val moodlexml = working + "_MoodleImport/moodle.xml"

    // If that passes, then check for validity
    new File(moodlexml) should be ('exists)
    XML.load(moodlexml).label should be === "MOODLE_BACKUP"
  }

  it should "have the correct course_files" in {
    val coursefiles = working + "_MoodleImport/course_files"
    
    new File(coursefiles) should be ('exists)
    new File(coursefiles + "/syllabus") should be ('exists)
    new File(coursefiles + "/syllabus/syllabus.md") should be ('exists)
    new File(coursefiles + "/important_stuff") should be ('exists)
    new File(coursefiles + "/important_stuff/test1.txt") should be ('exists)
    new File(coursefiles + "/important_stuff/test2.txt") should be ('exists)
    new File(coursefiles + "/important_stuff/test3.txt") should be ('exists)
  }

  "Moodle Conversions" should "produce correct Labels" in {
    val doclabel = findByName("Documents") 

    val expected = 
    <MOD>
      <ID>1</ID>
      <MODTYPE>label</MODTYPE>
      <NAME>Documents</NAME>
      <CONTENT>Documents</CONTENT>
      <TIMEMODIFIED />
    </MOD>

    doclabel.toXML.toString should be === expected.toString
  }

  it should "produce correct SingleFiles" in {
    val syllabus = findByName("Syllabus") 

    val expected = 
    <MOD>
      <ID>2</ID>
      <MODTYPE>resource</MODTYPE>
      <NAME>Syllabus</NAME>
      <TYPE>file</TYPE>
      <REFERENCE>syllabus/syllabus.md</REFERENCE>
      <SUMMARY>Syllabus</SUMMARY>
      <ALLTEXT></ALLTEXT>
      <POPUP></POPUP>
      <OPTIONS></OPTIONS>
      <TIMEMODIFIED />
    </MOD>
    syllabus.toXML.toString should be === expected.toString
  } 

  it should "produce correct OnlineDocuments" in {
    val online = findByName("Web Page") 

    val expected =
    <MOD>
      <ID>3</ID>
      <MODTYPE>resource</MODTYPE>
      <NAME>Web Page</NAME>
      <TYPE>text</TYPE>
      <REFERENCE>1</REFERENCE>
      <SUMMARY>Web Page</SUMMARY>
      <ALLTEXT>$@FILEPHP@$$@SLASH@$web_page/</ALLTEXT>
      <POPUP/>
      <OPTIONS/>
      <TIMEMODIFIED />
    </MOD>
    online.toXML.toString should be === expected.toString
  }

  it should "produce correct Directories" in {
    val directory = findByName("Important Stuff") 

    val expected =
    <MOD>
      <ID>4</ID>
      <MODTYPE>resource</MODTYPE>
      <NAME>Important Stuff</NAME>
      <TYPE>directory</TYPE>
      <REFERENCE>important_stuff</REFERENCE>
      <SUMMARY>Important Stuff</SUMMARY>
      <ALLTEXT></ALLTEXT>
      <POPUP/>
      <OPTIONS/>
      <TIMEMODIFIED />
    </MOD>

    directory.toXML.toString should be === expected.toString
  }

  it should "produce correct ExternalLinks" in {
    val external = findByName("Blog") 

    val expected = 
    <MOD>
      <ID>5</ID>
      <MODTYPE>resource</MODTYPE>
      <NAME>Blog</NAME>
      <TYPE>file</TYPE>
      <REFERENCE>http://philcalicode.blogspot.com</REFERENCE>
      <SUMMARY>Blog</SUMMARY>
      <ALLTEXT></ALLTEXT>
      <POPUP/>
      <OPTIONS/>
      <TIMEMODIFIED />
    </MOD>

    external.toXML.toString should be === expected.toString
  }
  
  it should "produce correct StaffInformation" in {
    val staff = findByName("Pro. Philip Cali")

    def html = {
      <h1 style="text-align: center">Mr. Philip Cali</h1>
      <table>
        <tr>
          <td></td>
          <td>
            <ul style="list-style-type: none;">
              <li>Name: Mr. Philip Cali</li>
              <li>Email: calico.software@gmail.com</li>
              <li>Phone: </li>
              <li>Office: </li>
            </ul>
          </td>
        </tr>
      </table>
    }

    val expected =
    <MOD>
      <ID>6</ID>
      <MODTYPE>resource</MODTYPE>
      <NAME>Pro. Philip Cali</NAME>
      <TYPE>html</TYPE>
      <REFERENCE></REFERENCE>
      <SUMMARY>Pro. Philip Cali</SUMMARY>
      <ALLTEXT>{ html.mkString }</ALLTEXT>
      <POPUP/>
      <OPTIONS/>
      <TIMEMODIFIED />
    </MOD>

    staff.toXML.toString should be === expected.toString
  }
}
