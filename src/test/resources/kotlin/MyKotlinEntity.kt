package sample

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MyKotlinEntity (
  var aLongVar: Long = 0
) : MyJavaEntity()
