package org.example;

import io.ebean.Ebean;
import org.example.domain.MyEntityJava;
import org.example.domain.MyTestEntityJava;
import org.example.domain.query.QMyEntityJava;
import org.example.domain.query.QMyTestEntityJava;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaTest {

  @Test
  public void saveMainEntity() {
    MyEntityJava myEntityJava = new MyEntityJava();
    myEntityJava.setName("hello");
    Ebean.save(myEntityJava);

    Optional<MyEntityJava> foundEntity = new QMyEntityJava().name.eq("hello").findOneOrEmpty();

    assertTrue(foundEntity.isPresent(), "Did not find the saved entity");
    assertEquals("hello", foundEntity.get().getName());
  }

  @Test
  public void saveTestEntity() {
    MyTestEntityJava myTestEntityJava = new MyTestEntityJava();
    myTestEntityJava.setName("hello");
    Ebean.save(myTestEntityJava);

    Optional<MyTestEntityJava> foundEntity = new QMyTestEntityJava().name.eq("hello").findOneOrEmpty();

    assertTrue(foundEntity.isPresent(), "Did not find the saved entity");
    assertEquals("hello", foundEntity.get().getName());
  }

}
