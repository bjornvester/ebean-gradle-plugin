package org.example.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Customer : MappedSuper2() {

  var name: String = ""

  var version: Long = 0
}
