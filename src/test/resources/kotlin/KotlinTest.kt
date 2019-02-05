package kotlin

import io.ebean.Ebean
import org.example.domain.query.QCustomer
import org.testng.annotations.Test

TODO

class KotlinTest {


  @Test
  fun save() {

    val customer = Customer()
    customer.name = "hello"

    Ebean.save(customer)


    val cust = QCustomer()
      .name.eq("hello")
      .findOneOrEmpty()

    cust.ifPresent {
      println("name:${it.name}")
    }

    println(cust)
  }
}
