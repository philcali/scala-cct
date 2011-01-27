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
package com.philipcali.cct

import java.util.zip._
import grizzled.util.{withCloseable => withc}
import Utils.readStream

/**
 * Handles all the extraction and archival needs of conversions
 * @author Philip Cali
 */
object Zip {
  private def append(dest: String) = if(dest.endsWith("/")) dest else dest + "/"

  private def newFolderFromName(name: String) {
    val folder = new java.io.File(name)
    if(!folder.exists) folder.mkdirs
  }

  private def testFolderInName(name: String) {
    if(name.contains("/")) {
      newFolderFromName(name.split("/").dropRight(1).mkString("/"))
    }
  }

  private def readZip(is: ZipInputStream, dest: String) {
    is.getNextEntry match {
      case zipEntry: ZipEntry => {
        val name = zipEntry.getName
        val zippedFile = new java.io.File(dest + name)

        // Create dir if this is one
        if(zipEntry.isDirectory) {
          if(!zippedFile.exists) zippedFile.mkdir
        } else {
          // Make folders if it's in the name
          testFolderInName(dest + name)

          // Write File
          withc(new java.io.FileOutputStream(dest + name)) { out =>
            readStream(is, out)
          }
          is.closeEntry
        }
        // Read some more
        readZip(is, dest)
      }
      case _ => is.close
    } 
  }

  /**
   * Takes a zip full path, and a possible destination, and extracts
   *
   * Ex: {{{ extract("archive.zip") // yields a directory archive in "." }}}
   * Ex: {{{ extract("archive.zip", "temp/archives") // yields temp/archives/archive }}}
   */
  def extract(file: String, dest: String = "./") = {
    newFolderFromName(dest) 
    val archive = new java.io.File(file)
    val zipin = new ZipInputStream(new java.io.FileInputStream(archive))

    // Let's read it
    val realDest = append(dest) + archive.getName.split("\\.")(0) + "/"
    readZip(zipin, realDest)
  }

  private def zip(out: ZipOutputStream, parent: Option[ZipEntry], file: java.io.File) {
    val appender = if(file.isDirectory) file.getName + "/" else file.getName

    val newEntry = parent match {
      case Some(entry) => new ZipEntry(entry.getName + appender) 
      case None => new ZipEntry("./")
    }
   
    // Put entry in zip
    out.putNextEntry(newEntry)

    if(file.isDirectory) {
      val children = file.listFiles.filter(f => !f.getName.startsWith("."))
      for(child <- children) (zip(out, Some(newEntry), child))
    } else {
      withc(new java.io.FileInputStream(file)) { fis =>
        readStream(fis, out)
      }
    }
    out.closeEntry
  }

  /**
   * Archives a directory as a ".zip"
   *
   * Ex: {{{ archive("archive") // yields "archive.zip" }}}
   * Ex: {{{ archive("archive", "temp/archives") // yields temp/archives/archive.zip }}}
   */
  def archive(dir: String, dest: String = "./") = {
    val dirname = new java.io.File(dir)
    
    withc(new ZipOutputStream(new java.io.FileOutputStream(append(dest) + dirname.getName + ".zip"))) { zipout =>
      zip(zipout, None, dirname)
    }
  }
}