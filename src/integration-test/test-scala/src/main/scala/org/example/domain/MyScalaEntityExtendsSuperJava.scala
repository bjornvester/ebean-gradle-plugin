package org.example.domain

import javax.persistence.{Entity, Id}

@Entity
class MyScalaEntityExtendsSuperJava extends MappedSuperJava {
  @Id
  var id: Long = 0
}
