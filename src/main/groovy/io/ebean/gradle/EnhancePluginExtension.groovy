package io.ebean.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Configuration options for the Ebean gradle plugin.
 */
//@CompileStatic
class EnhancePluginExtension {

  /**
   * Ebean enhancer debug level. Can be from 0-9.
   */
  int debugLevel = 0

  /**
   * When true registers Java querybean generation.
   */
  boolean queryBeans = false

  /**
   * For backwards compatibility only. Not used any more.
   */
  @Deprecated
  boolean kotlin = false

  /**
   * ebean version to include as a dependency to the enhanced classes.
   */
  String ebeanVersion = '11.33.3'

  /**
   * querybean-generator version for use when generating query beans.
   */
  String generatorVersion = '11.27.1'

  /**
   * ebean-querybean version for use when generating query beans.
   */
  String querybeanVersion = "11.33.3"

  /**
   * Location of the EBean configuration file (if present).
   */
  File configurationFile

  /**
   * If you use IntelliJ IDEA and has querybean generation enabled, IDEA will by default not discover the generated sources.
   * Set this property to true to make the plugin configure IDEA for this.
   * This is a work-around until https://youtrack.jetbrains.com/issue/IDEA-187868 is solved.
   */
  boolean configureIdeaModulesForQuerybeanGeneration = true

  EnhancePluginExtension(Project project) {
    configurationFile = project.file("src/main/resources/ebean.mf")
    greeting = project.objects.property(String)
  }

  Property<String> greeting

}
