package io.ebean.gradle

import groovy.transform.CompileStatic


@CompileStatic
class EnhanceException extends RuntimeException {

  EnhanceException(String message, Throwable cause) {
    super(message, cause)
  }
}
