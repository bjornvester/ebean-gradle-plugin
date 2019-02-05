package org.example.domain

import javax.persistence.Entity

@Entity
class MyKotlinTestEntity : MappedSuperKotlin() {
  var name: String? = ""
}
