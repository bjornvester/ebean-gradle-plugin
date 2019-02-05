package org.example.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MyScalaTestEntity {
  @Id
  var id: Long = 0
}
