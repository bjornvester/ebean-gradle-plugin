package org.example

import io.ebean.Ebean
import org.example.domain.MyEntityKotlin
import org.example.domain.MyEntityKotlinExtendsSuperJava
import org.example.domain.MyKotlinTestEntity
import org.example.domain.query.QMyKotlinTestEntity
import org.example.domain.query.QMyEntityKotlin
import org.example.domain.query.QMyEntityKotlinExtendsSuperJava
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KotlinTest {

  @Test
  fun `main entities can be saved`() {
    val myEntityKotlin = MyEntityKotlin()
    myEntityKotlin.name = "hello"

    Ebean.save(myEntityKotlin)

    val foundEntity = QMyEntityKotlin().name.eq("hello").findOneOrEmpty()
    assertTrue(foundEntity.isPresent(), "Did not find the saved entity")
    assertEquals("hello", foundEntity.get().name)
  }

  @Test
  fun `main entities with Java as superclasses can be saved`() {
    val myEntityKotlin = MyEntityKotlinExtendsSuperJava()
    myEntityKotlin.name = "hello"

    Ebean.save(myEntityKotlin)

    val foundEntity = QMyEntityKotlinExtendsSuperJava().name.eq("hello").findOneOrEmpty()
    assertTrue(foundEntity.isPresent(), "Did not find the saved entity")
    assertEquals("hello", foundEntity.get().name)
  }

  @Test
  fun `test entities can be saved`() {
    val myEntityKotlin = MyKotlinTestEntity()
    myEntityKotlin.name = "hello"

    Ebean.save(myEntityKotlin)

    val foundEntity = QMyKotlinTestEntity().name.eq("hello").findOneOrEmpty()
    assertTrue(foundEntity.isPresent(), "Did not find the saved entity")
    assertEquals("hello", foundEntity.get().name)
  }
}
