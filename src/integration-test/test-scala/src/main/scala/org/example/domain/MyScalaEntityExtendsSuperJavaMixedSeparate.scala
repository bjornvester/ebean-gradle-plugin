package org.example.domain

import javax.persistence.{Column, Entity, Id}

import scala.beans.BeanProperty

@Entity
class MyScalaEntityExtendsSuperJavaMixedSeparate extends MappedSuperJavaMixedSeparate {
  @BeanProperty
  var name: String = null
}
