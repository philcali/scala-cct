package com.philipcali.cct


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

  def remove(dir: String) {
    recurse(new java.io.File(dir))(_.delete)
  }
}
