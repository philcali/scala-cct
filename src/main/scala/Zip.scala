package com.philipcali.cct

import java.util.zip._
import grizzled.util.{withCloseable => withc}

object Zip {

  private def readStream(is: java.io.InputStream, to: java.io.OutputStream) {
    val buf = new Array[Byte](1024)

    is.read(buf, 0, 1024) match {
      case n if(n > -1) => to.write(buf, 0, n); readStream(is, to)
      case _ =>
    }
  }

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

  def extract(file: String, dest: String = "./") = {
    newFolderFromName(dest) 
    val archive = new java.io.File(file)
    val zipin = new ZipInputStream(new java.io.FileInputStream(archive))

    // Let's read it
    val realDest = (if(!dest.endsWith("/")) dest + "/" else dest) + 
                    archive.getName.split("\\.")(0) + "/"
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

  def archive(dir: String, dest: String = "./") = {
    val dirname = new java.io.File(dir)
    
    withc(new ZipOutputStream(new java.io.FileOutputStream(dest + dirname.getName + ".zip"))) { zipout =>
      zip(zipout, None, dirname)
    }
  }
}
