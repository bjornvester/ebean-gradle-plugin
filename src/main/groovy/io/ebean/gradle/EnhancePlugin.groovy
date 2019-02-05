package io.ebean.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.GradleVersion

import static org.gradle.api.tasks.PathSensitivity.NONE

@CompileStatic
class EnhancePlugin implements Plugin<Project> {
  private final Logger logger = Logging.getLogger(EnhancePlugin.class)

  // TODO: Test that the project can be imported in IntelliJ if cleaned before hand, and that the generated source directories appear when generating the queries.
  // TODO: Consider moving the integration-test folder from src to the root
  // TODO: Cleanup or ignore generated SQL files from the unit tests
  // TODO: Make sure all examples in the following repository passes: https://github.com/ebean-orm-examples
  // TODO: See the following for inspiration on APT processing in 5.2 as well as the provider API: https://github.com/tbroyer/gradle-apt-plugin/commit/6e375fe2fa4d6498cf71a82530588ca15c95ab7f
  // TODO: Document the workaround for the following issue (which is do separate Java sources in a Kotlin project used as MappedSuper classes into its own project): https://github.com/ebean-orm-tools/ebean-gradle-plugin/issues/18
  // TODO: Verify that we use the new Task API: https://docs.gradle.org/4.9/userguide/task_configuration_avoidance.html#sec:old_vs_new_configuration_api_overview
  // TODO: See https://github.com/gradle-guides/gradle-site-plugin/blob/master/src/main/kotlin/org/gradle/plugins/site/SitePlugin.kt for best practices
  // TODO: Extension should probably use the properties/provider API
  // TODO: Should the logger be grabbed from the project instead (e.g. project.logger.debug(...))?
  // TODO: Test API generated folders in Eclipse
  // TODO: There should be an extension property for Kotlin query generation. This is speed up the build if there are no Java entity beans.
  // TODO: Will the Java query generator pick up Kotlin entity classes as they are (I think) on the classpath? And what happens if this is the case?
  // TODO: Verify that the query source generator has the output directory as a Gradle output for up-to-date checking
  // TODO: Document how to enable Kotlin query beans (e.g. you have to manually apply kapt dependencies)
  // TODO: If we introduce the "kotlin query beans flag" in the extension, test that the kapt dependencies have been added manually (or do it automatically in a listener/setter method in the configuration phase - if so, do we need to do it differently if the Kotlin plugin has not been applied yet?).
  void apply(Project project) {
    if (GradleVersion.current() < GradleVersion.version("4.9")) {
      throw new GradleException("The Ebean Gradle plugin is only supported in Gradle version 4.9 and higher")
    }

    EnhancePluginExtension extension = project.extensions.create("ebean", EnhancePluginExtension, project)

    // TODO: NOT LAZY IT SEEMS - see: https://github.com/hibernate/hibernate-orm/blob/master/tooling/hibernate-gradle-plugin/src/main/groovy/org/hibernate/orm/tooling/gradle/HibernatePlugin.java

    project.afterEvaluate { // This is to ensure the extension properties have been evaluated (if present)
      File configurationFile = extension.configurationFile

      if (configurationFile.name != "ebean.mf") {
        throw new GradleException("The name of the configuration file must be ebean.mf")
      }

      if (extension.queryBeans && extension.configureIdeaModulesForQuerybeanGeneration) {
        supportAptSourcesInIdea(project)
      }

      project.tasks.withType(AbstractCompile).configureEach({ AbstractCompile compileTask ->
        // The following avoids the unwanted tasks kaptGenerateStubsKotlin and kaptGenerateStubsTestKotlin
        if (compileTask.name.startsWith("compile")) {

          // Input is optional as the configuration file might not exist
          // It is also not path sensitive as it is a configuration file (if we don't declare this, the task output will not be relocatable in the cache)
          compileTask.inputs.files(configurationFile).optional().withPathSensitivity(NONE)

          compileTask.doLast {
            Set<File> extraClassPath = compileTask.classpath.files + compileTask.destinationDir + configurationFile.parentFile
            URL[] extraClasspathAsUrlArray = extraClassPath*.toURI()*.toURL() as URL[]

            // TODO: Temporary only (hopefully)
            // Workaround for https://github.com/ebean-orm/ebean-agent/issues/93
            extraClasspathAsUrlArray.find { it.path.endsWith("jar") }?.openConnection()?.with { con ->
              con.setDefaultUseCaches(false)
            }

            ClassLoader cxtLoader = Thread.currentThread().getContextClassLoader()
            logger.debug("Performing enhancement for task $compileTask.name on folder $compileTask.destinationDir")
            new EbeanEnhancer(compileTask.destinationDir.toPath(), extraClasspathAsUrlArray, cxtLoader, extension).withCloseable {
              it.enhance()
            }
          }
        }
      } as Action<AbstractCompile>)

      project.plugins.withType(JavaPlugin) {
        // TODO: Find out if we want this or not
        //project.dependencies.add('implementation', "io.ebean:ebean-querybean:$extension.ebeanVersion")
      }

      if (extension.queryBeans) {
        project.plugins.withType(JavaPlugin) {
          // TODO: Test if this works for version >= 5.2 (e.g. snapshot of master branch if still unreleased). See https://github.com/gradle/gradle/pull/7551/files for some of the details.
          if (GradleVersion.current() < GradleVersion.version("5.2")) {
            // Workaround for https://github.com/gradle/gradle/issues/4956
            SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets")
            sourceSets.configureEach({ SourceSet sourceSet ->
              if (sourceSet.compileJavaTaskName) {
                project.tasks.named(sourceSet.compileJavaTaskName).configure({ JavaCompile task ->
                  task.options.annotationProcessorGeneratedSourcesDirectory = project.file("$project.buildDir/generated/sources/annotationProcessor/java/${sourceSet.name}")
                } as Action)
              }
            } as Action<SourceSet>)
          }

          DependencyHandler dependencies = project.dependencies

          // For the "main" source set
          dependencies.add('annotationProcessor', "io.ebean:querybean-generator:$extension.generatorVersion")
          dependencies.add('annotationProcessor', "io.ebean:persistence-api:2.2.1")
          dependencies.add('annotationProcessor', "io.ebean:ebean-annotation:4.3")
          dependencies.add('implementation', "io.ebean:ebean-querybean:$extension.querybeanVersion")

          // For the "test" source set
          dependencies.add('testAnnotationProcessor', "io.ebean:querybean-generator:$extension.generatorVersion")
          dependencies.add('testAnnotationProcessor', "io.ebean:persistence-api:2.2.1")
          dependencies.add('testAnnotationProcessor', "io.ebean:ebean-annotation:4.3")
          dependencies.add('testImplementation', "io.ebean:ebean-querybean:$extension.querybeanVersion")
        }
      }
    }
  }

  /**
   * Workaround for https://youtrack.jetbrains.com/issue/IDEA-187868.
   */
  @CompileDynamic
  private void supportAptSourcesInIdea(Project project) {
    project.plugins.withType(JavaPlugin) {
      if (project.plugins.hasPlugin("scala")) {
        // For what-ever reason, there are problems with the Scala and Idea plugins in combination in a sub-project
        // Applying the Idea plugin to the root project seems to work around the problem
        project.rootProject.plugins.apply("idea")
      }
      project.plugins.apply("idea")
      project.afterEvaluate {
        project.idea.module {
          def mainGeneratedSources = project.tasks["compileJava"].options.annotationProcessorGeneratedSourcesDirectory
          if (mainGeneratedSources) {
            sourceDirs += mainGeneratedSources
            generatedSourceDirs += mainGeneratedSources
          }
          def testGeneratedSources = project.tasks["compileTestJava"].options.annotationProcessorGeneratedSourcesDirectory
          if (testGeneratedSources) {
            testSourceDirs += testGeneratedSources
            generatedSourceDirs += testGeneratedSources
          }
        }
      }
    }
  }
}
