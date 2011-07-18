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
package com.github.philcali.cct

package finder

import transformer._
import knowledge._
import system._

import java.lang.Class
import org.clapper.classutil.ClassFinder
  

/**
 * Finds Knowledges and Transformers for the main
 * converter.
 * @author Philip Cali
 */
object MetaFinder {
  lazy val tags = {
    val classes = ClassFinder().getClasses.filter(_.name.contains("Tag")).toList

    List("Knowledge", "Transformer").map { typ =>
      ClassFinder.concreteSubclasses("com.github.philcali.cct.system."+typ+"Tag", classes.iterator).map { x => 
        val clazz = Class.forName(x.name)
        clazz.newInstance.asInstanceOf[MetaTag]
      }.toList
    }.flatten
  }
  
  def find[A](tpe: String, name: String, pack: String = "com.github.philcali.cct") = {
    val className = pack + "." + name + "." + name(0).toUpper + name.drop(1) + tpe
    Class.forName(className).asInstanceOf[Class[A]]
  }  

  def knowledgeTag(name: String, pack: String = "com.github.philcali.cct") = {
    val clazz = find("Knowledge", name, pack)
    clazz.getMethod("tag").invoke(clazz).asInstanceOf[KnowledgeTag]
  }

  def transformerTag(name: String, pack: String = "com.github.philcali.cct") = {
    val clazz = find("Transformer", name, pack)
    clazz.getMethod("tag").invoke(clazz).asInstanceOf[TransformerTag]
  }

  def knowledge(name: String, pack: String, con: String*): Knowledge = {
    val clazz = find("Knowledge", name, pack)
    clazz.getConstructor(con.map(c => classOf[String]):_*).newInstance(con:_*)
  }

  def transformer(name: String, pack: String, con: String*): Transformer = { 
    val clazz = find("Transformer", name, pack)
    clazz.getConstructor(con.map(c => classOf[String]):_*).newInstance(con:_*)
  }
}
