package org.example.domain

import javax.persistence.{Entity, Id}

import scala.beans.BeanProperty

@Entity
class MyScalaEntityExtendsSuperJavaMixedSame extends MappedSuperJavaMixedSame {
  @BeanProperty
  var name: String = null
}
