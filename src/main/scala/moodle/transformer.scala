package com.philipcali.cct
package moodle

import transformer._
import system.{TransformerTag, Tagger}
import Zip.archive
import MoodleConversions._
import course._
import Utils.remove

import java.util.Date
import grizzled.util.{withCloseable => withc}

/**
 * @author Philip Cali
 **/
class MoodleTag extends TransformerTag {
  def name = "moodle"
  def suffix = "_MoodleImport"
  def version = "1.8 < 2.0"
}

/**
 * @author Philip Cali
 **/
object MoodleTransformer extends Tagger[MoodleTag] {
  def tag = new MoodleTag
  def apply(working: String, output: String= "./") = 
    new MoodleTransformer(working, output)
}

/**
 * @author Philip Cali
 **/
class MoodleTransformer(val working: String, val output: String) extends Transformer {
  def this(working: String) = this(working, "./")
  
  def infoMods = List(("label", ((m: Module) => m.isInstanceOf[Label])),
                      ("resource", ((m: Module) => m.isInstanceOf[Resource])),
                      ("quiz", ((m: Module) => m.isInstanceOf[Quiz])))

  def staging = {
    val mainDir = new java.io.File(working.split("/").last + "_MoodleImport")
    
    if(!mainDir.exists) {
      mainDir.mkdir
      
      new java.io.File(mainDir.getName + "/course_files").mkdir
      new java.io.File(mainDir.getName + "/site_files").mkdir
    }

    mainDir.getName
  }

  /**
   * Cleans up staging directory
   */
  def cleanup() = {
    remove(working)
    remove(staging)
  }

  def transform(course: Course) = {    

    // Move all content to the appropriate staging area
    course.details filter(m => !m.isInstanceOf[Section]) foreach { mod => 
      mod.transform(working, staging)
    }

    def courseXml = { 
    <MOODLE_BACKUP>
      <INFO>
        <NAME>backup-xxx.zip</NAME>
        <MOODLE_VERSION>2007101501</MOODLE_VERSION>
        <MOODLE_RELEASE>1.8</MOODLE_RELEASE>
        <BACKUP_VERSION>2007101000</BACKUP_VERSION>
        <BACKUP_RELEASE>1.8</BACKUP_RELEASE>
        <DATE>{ new Date().getTime }</DATE>
        <ORIGINAL_WWWROOT>http://philcalicode.blogspot.com</ORIGINAL_WWWROOT>
        <ZIP_METHOD>internal</ZIP_METHOD>
        <DETAILS>
            { infoMods.map { tupes =>
                val instances = course.details.filter(tupes._2)
                if(instances.size > 0) {
                  <MOD>
                    <NAME>{ tupes._1 }</NAME>
                    <INCLUDED>true</INCLUDED>
                    <USERINFO>false</USERINFO>
                    <INSTANCES> {
                       instances map { instance =>
                        <INSTANCE>
                        <ID>{ instance.id }</ID>
                        <NAME>{ instance.name }</NAME>
                        <INCLUDED>true</INCLUDED>
                        <USERINFO>false</USERINFO>
                        </INSTANCE>
                        }
                     }
                    </INSTANCES>
                  </MOD>
                 }
                else ""
              }
            }
          <METACOURSE>false</METACOURSE>
          <USERS>none</USERS>
          <LOGS>false</LOGS>
          <USERFILES>false</USERFILES>
          <COURSEFILES>true</COURSEFILES>
          <SITEFILES>true</SITEFILES>
          <GRADEBOOKHISTORIES>false</GRADEBOOKHISTORIES>
          <MESSAGES>false</MESSAGES>
          <BLOGS>false</BLOGS>
        </DETAILS>
      </INFO>
      <ROLES></ROLES>
      <COURSE>
        <HEADER>
          <ID></ID>
          <CATEGORY>
            <ID>1</ID>
            <NAME>Miscellaneous</NAME>
          </CATEGORY>
          <PASSWORD></PASSWORD>
          <FULLNAME>{ course.info.title }</FULLNAME>
          <SHORTNAME>{ course.info.title.split(" ").mkString }</SHORTNAME>
          <IDNUMBER></IDNUMBER>
          <SUMMARY>{ course.info.description }</SUMMARY>
          <FORMAT>topics</FORMAT>
          <SHOWGRADES>1</SHOWGRADES>
          <NEWSITEMS>1</NEWSITEMS>
          <TEACHER>Teacher</TEACHER>
          <TEACHERS>Teachers</TEACHERS>
          <STUDENT>Student</STUDENT>
          <STUDENTS>Students</STUDENTS>
          <GUEST>0</GUEST>
          <STARTDATE>1282539600</STARTDATE>
          <NUMSECTIONS>5</NUMSECTIONS>
          <MAXBYTES>0</MAXBYTES>
          <SHOWREPORTS>0</SHOWREPORTS>
          <GROUPMODE>0</GROUPMODE>
          <GROUPMODEFORCE>0</GROUPMODEFORCE>
          <DEFAULTGROUPINGID>0</DEFAULTGROUPINGID>
          <LANG></LANG>
          <THEME></THEME>
          <COST></COST>
          <CURRENCY>USD</CURRENCY>
          <MARKER>0</MARKER>
          <VISIBLE>1</VISIBLE>
          <HIDDENSECTIONS>0</HIDDENSECTIONS>
          <TIMECREATED></TIMECREATED>
          <TIMEMODIFIED></TIMEMODIFIED>
          <METACOURSE>0</METACOURSE>
          <EXPIRENOTIFY>0</EXPIRENOTIFY>
          <NOTIFYSTUDENTS>0</NOTIFYSTUDENTS>
          <EXPIRYTHRESHOLD>864000</EXPIRYTHRESHOLD>
          <ENROLLABLE>1</ENROLLABLE>
          <ENROLSTARTDATE>0</ENROLSTARTDATE>
          <ENROLENDDATE>0</ENROLENDDATE>
          <ENROLPERIOD>0</ENROLPERIOD>
          <ROLES_OVERRIDES>
          </ROLES_OVERRIDES>
          <ROLES_ASSIGNMENTS>
          </ROLES_ASSIGNMENTS>
        </HEADER>
        <BLOCKS/>
        <SECTIONS>
          { course.sections.filter(_.children.size != 0).zipWithIndex.map { entry =>
              val (section, index) = entry
              <SECTION>
                <ID>{ index + 2 }</ID>
                <NUMBER>{ index + 1 }</NUMBER>
                <SUMMARY>{ section.wrapped.name }</SUMMARY>
                <VISIBLE>1</VISIBLE>
                <MODS>
                  { course.mods(section).zipWithIndex.map { modEntry =>
                     val (mod, modID) = modEntry
                     <MOD>
                      <ID>{ mod.wrapped.id }</ID>
                      <TYPE>{ determine(mod.wrapped) }</TYPE>
                      <INSTANCE>{ mod.wrapped.id }</INSTANCE>
                      <ADDED/>
                      <SCORE>0</SCORE>
                      <INDENT>{ mod.level - 1 }</INDENT>
                      <VISIBLE>1</VISIBLE>
                      <GROUPMODE>0</GROUPMODE>
                      <GROUPINGID>0</GROUPINGID>
                      <GROUPMEMBERSONLY>0</GROUPMEMBERSONLY>
                      <IDNUMBER>$@NULL@$</IDNUMBER>
                      <ROLES_OVERRIDES>
                      </ROLES_OVERRIDES>
                      <ROLES_ASSIGNMENTS>
                      </ROLES_ASSIGNMENTS>
                    </MOD>
                    }
                  }
                </MODS>
              </SECTION>
            } 
          }
        </SECTIONS>
        <QUESTION_CATEGORIES>
          { course.nondisplay.filter { cat => 
              cat.isInstanceOf[QuestionCategory] && 
              cat.asInstanceOf[QuestionCategory].questions.size > 0 
            } map { category =>
             category.toXML 
            } 
          }
        </QUESTION_CATEGORIES>
        <GROUPS/>
        <GRADEBOOK/>
        <MODULES>
          { course.details.filter(m => !m.isInstanceOf[Section]).map { mod =>
              mod.toXML
            }
          }
        </MODULES>
        <FORMATDATA />
      </COURSE>
    </MOODLE_BACKUP>
    }

    // Moodle require double instead of single quotes
    withc(new java.io.FileWriter(staging + "/moodle.xml")) { writer =>
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
      writer.write(courseXml.mkString)
    }

    // Create a Moodle archive
    archive(staging, output)

    // Cleanup crap
    cleanup()
  }

  def determine(m: Module) = m match {
    case l: Label => "label"
    case r: Resource => "resource"
    case q: Quiz => "quiz"
    case _ => "label"
  }

}

