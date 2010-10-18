package com.philipcali.cct

package dump

import course._

object DumpConversions {
  implicit def module2DumpModule(m: CourseModule) = m.wrapped match {
    case l: Label => new DumpLabel(m, l)
    case s: SingleFile => new DumpSingleFile(m, s)
    case d: Directory => new DumpDirectory(m, d)
    case _ => new DumpUnknown(m, m.wrapped)
  }
}

trait DumpModule {
  import DumpConversions._

  val under: CourseModule
  val module: Module

  def tpe = module.getClass.getSimpleName

  def transform(working: String, staging: String) = {
    val oldDir = new JFile(working + "/" + module.from)
    if(oldDir.exists) {
      val newDir = new JFile(staging + "/" + module.from)

      copy(oldDir, newDir)
    }
  }

  def extraXML = <EXTRA />

  def toXML: scala.xml.Node = {
    <MODULE>
      <ID>{ module.id }</ID>
      <LEVEL>{ under.level }</LEVEL>
      <TYPE>{ tpe }</TYPE>
      <NAME>{ module.name }</NAME>
      <REFERENCE>{ module.from }</REFERENCE>
      { extraXML }
      <MODULES>{ under.children.map(_.toXML) }</MODULES>
    </MODULE>
  }
}

trait FileHandler {
  def fileXml(file: File) = {
    <FILE>
      <NAME>{ file.name }</NAME>
      <LINKNAME>{ file.linkname }</LINKNAME>
      <SIZE>{ file.size }</SIZE>
    </FILE>
  }  
}

class DumpUnknown(val under: CourseModule, val module: Module) extends DumpModule {
  override def tpe = "unsupported"
}

class DumpLabel(val under: CourseModule,val module: Label) extends DumpModule

class DumpSingleFile(val under: CourseModule,val module: SingleFile) extends DumpModule with FileHandler{
  override def extraXML = fileXml(module.file)
}

class DumpDirectory(val under: CourseModule,val module: Directory) extends DumpModule with FileHandler {
  override def extraXML = {
    <FILES>{ module.directory.map(fileXml) }</FILES>
  }
}

class DumpOnline(val under: CourseModule, val module: OnlineDocument) extends DumpModule {
  override def extraXML = {
    <TEXT>{ module.text }</TEXT>
  }
}
