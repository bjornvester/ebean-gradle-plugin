package org.example.domain

import javax.persistence.Entity

@Entity
class MyEntityKotlin : MappedSuperKotlin() {
  var name: String? = ""
}
