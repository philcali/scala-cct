package com.philipcali.cct

package finder

import transformer._
import knowledge._
import system._

import java.lang.Class

object MetaFinder {
  
  def find[A](tpe: String, name: String, pack: String = "com.philipcali.cct") = {
    val className = pack + "." + name + "." + name(0).toUpper + name.drop(1) + tpe
    Class.forName(className).asInstanceOf[Class[A]]
  }  

  def knowledgeTag(name: String, pack: String = "com.philipcali.cct") = {
    val clazz = find("Knowledge", name, pack)
    clazz.getMethod("tag").invoke(clazz).asInstanceOf[KnowledgeTag]
  }

  def transformerTag(name: String, pack: String = "com.philipcali.cct") = {
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

