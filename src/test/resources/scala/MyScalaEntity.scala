package sample

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MyScalaEntity {
  @Id
  var id: Long = 0
}
