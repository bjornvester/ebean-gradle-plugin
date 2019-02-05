package org.example.domain

import javax.persistence.Entity
import javax.persistence.Id

import scala.beans.BeanProperty

@Entity
class MyScalaEntity extends MappedSuperScala {
  @BeanProperty
  var name: String = null
}
