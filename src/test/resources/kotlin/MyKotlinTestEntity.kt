package sample

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MyKotlinTestEntity(
  @Id
  var id: Long = 0
)
