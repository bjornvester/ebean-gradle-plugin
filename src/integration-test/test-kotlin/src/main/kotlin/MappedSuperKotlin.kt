package org.example.domain

import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
open class MappedSuperKotlin {
  @Id
  var id: Long? = 0L
}
