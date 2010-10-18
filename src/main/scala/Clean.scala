package com.philipcali.cct

import grizzled.util.{withCloseable => withc}

package object clean {
  lazy val offensive = List("'", "\"", "\\.", "!", "\\?", "&", ",", 
                       "@", "\\$", "\\(", "\\)", "\\[", "\\]", 
                       "\\\\", "/", "\\|", "\\*")

  def lower(str: String) = str.toLowerCase.split(" ").mkString("_")

  def dirclean(str: String) = prune(lower(str))

  def fileclean(str: String) = prune(lower(str), "\\.")

  def prune(str: String, exception: String = "") = {
    offensive.filter(c => !c.equals(exception)).foldLeft(str){(in, char) =>
      in.replaceAll(char, "")
    }
  }

  def recurse(file: java.io.File)(action: java.io.File => Unit) {
    if(file.isDirectory) {
      file.listFiles.foreach(f=> recurse(f)(action))
    }
    action(file)
  }

  def readStream(in: java.io.InputStream, out: java.io.OutputStream) {
    val buffer = new Array[Byte](1024)
    in.read(buffer) match {
      case n if(n != -1) => out.write(buffer, 0, n); readStream(in, out)
      case _ =>
    }
  }

  def copyFile(oldFile: String, newFile: String) {
    withc(new java.io.FileInputStream(oldFile)) { in =>
      withc(new java.io.FileOutputStream(newFile)) { out =>
        readStream(in, out)
      }
    }
  }

  def copy(oldDir: java.io.File, newDir: java.io.File) {
    newDir.mkdir

    recurse(oldDir) { file => 
      if(file.isFile) {
        copyFile(file.getAbsolutePath, newDir.getAbsolutePath + "/" + file.getName)
      }
    }
  }

  def remove(dir: String) {
    recurse(new java.io.File(dir))(_.delete)
  }
}
