package org.example.domain

import javax.persistence.Entity

@Entity
class MyEntityKotlinExtendsSuperJava : MappedSuperJava() {
  var name: String? = ""
}
