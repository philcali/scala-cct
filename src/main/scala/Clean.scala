package com.philipcali.cct

import grizzled.util.{withCloseable => withc}

/**
 * Contains various folder and file utilities during the conversion process.
 * @author Philip Cali
 */
object Utils {
  lazy val offensive = List("'", "\"", "\\.", "!", "\\?", "&", ",", 
                       "@", "\\$", "\\(", "\\)", "\\[", "\\]", 
                       "\\\\", "/", "\\|", "\\*")

  /**
   * Transforms a line of text to lower case and underscores for spaces
   *
   * Ex: {{{ lower("The Moon Is Down") // the_moon_is_down }}}
   */
  def lower(str: String) = str.toLowerCase.split(" ").mkString("_")

  /**
   * Transforms a stringwith potential offensive chars into one without it
   *
   * Ex: {{{ dirclean("The Moon's Down") // the_moons_down }}}
   */
  def dirclean(str: String) = prune(lower(str))

  /**
   * Like {{{ dirclean }}} except used for files
   */
  def fileclean(str: String) = prune(lower(str), "\\.")

  def prune(str: String, exception: String = "") = {
    offensive.filter(c => !c.equals(exception)).foldLeft(str){(in, char) =>
      in.replaceAll(char, "")
    }
  }

  /**
   * Recursively looks through a directory, and does an action
   *
   * Ex: {{{ recurse(new File("."))(f => println(f.getName)) }}}
   */
  def recurse(file: java.io.File)(action: java.io.File => Unit) {
    if(file.isDirectory) {
      file.listFiles.foreach(f=> recurse(f)(action))
    }
    action(file)
  }

  /**
   * Reads all the stream data from an input to an output
   */
  def readStream(in: java.io.InputStream, out: java.io.OutputStream) {
    val buffer = new Array[Byte](1024)
    in.read(buffer) match {
      case n if(n > -1) => out.write(buffer, 0, n); readStream(in, out)
      case _ =>
    }
  }

  /**
   * Copies any given file from one path to the other
   */
  def copyFile(oldFile: String, newFile: String) {
    withc(new java.io.FileInputStream(oldFile)) { in =>
      withc(new java.io.FileOutputStream(newFile)) { out =>
        readStream(in, out)
      }
    }
  }

  /**
   * Copies an entire directory from one given path to another
   */
  def copy(oldDir: java.io.File, newDir: java.io.File) {
    newDir.mkdir
  
    recurse(oldDir) { file => 
      if(file.isFile) {
        copyFile(file.getAbsolutePath, newDir.getAbsolutePath + "/" + file.getName)
      }
    }
  }

  /**
   * Removes a directory
   * Ex: {{{ remove("temp") // like "rm -rf temp" in Unix }}}
   */
  def remove(dir: String) {
    recurse(new java.io.File(dir))(_.delete)
  }
}
