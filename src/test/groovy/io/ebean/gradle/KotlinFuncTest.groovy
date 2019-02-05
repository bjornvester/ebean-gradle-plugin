package io.ebean.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.*

class KotlinFuncTest extends BaseFuncTest {

  @Unroll
  def "The plugin can be applied to a Kotlin project for Gradle version #gradleVersionArg"() {
    given: "the Gradle version under test"
    gradleVersion = gradleVersionArg

    and: "a main entity class and a test entity class in Java"
    file("src/main/java/sample/MyJavaEntity.java") << getClass().getResource('/java/MyJavaEntity.java').text
    file("src/test/java/sample/MyJavaTestEntity.java") << getClass().getResource('/java/MyJavaTestEntity.java').text

    and: "a main entity class and a test entity class in Kotlin"
    file("src/main/kotlin/sample/MyKotlinEntity.kt") << getClass().getResource('/kotlin/MyKotlinEntity.kt').text
    file("src/test/kotlin/sample/MyKotlinTestEntity.kt") << getClass().getResource('/kotlin/MyKotlinTestEntity.kt').text

    and: "an ebean.mf configuration file"
    file("src/main/resources/ebean.mf") << getClass().getResource('/ebean.mf').text

    and: "a Gradle build file, applying the plugin to a Kotlin build of the project"
    String buildFileContent = getClass().getResource('/kotlin/build.gradle').text

    //buildFileContent = buildFileContent.replace("queryBeans = true", "queryBeans = false")

    buildFile << buildFileContent

    and: "a unit test that uses the compiled and enhanced classes"
    file("src/main/resources/kotlin/KotlinTest.kt")

    when: "running the build"
    def result = runWithArguments('-dS', '--warning-mode', 'all', 'build')

    then: "the build was successful"
    result.task(":build").outcome == SUCCESS

    and: "all four classes for Java, Kotlin, Main and Test were enhanced"
    result.output.contains('Enhanced sample/MyJavaEntity')
    result.output.contains('Enhanced sample/MyJavaTestEntity')
    result.output.contains('Enhanced sample/MyKotlinEntity')
    result.output.contains('Enhanced sample/MyKotlinTestEntity')

    and: "query beans were generated for both Java and Kotlin classes"
    //result.output.contains('Enhanced sample/query/assoc/QAssocMyJavaEntity')
    //result.output.contains('Enhanced sample/query/QMyJavaTestEntity$Alias')
    result.output.contains('Enhanced sample/query/assoc/QAssocMyKotlinEntity')
    //result.output.contains('Enhanced sample/query/assoc/QAssocMyKotlinTestEntity')

    when: "running the build a second time without changes"
    BuildResult resultSecondRun = runWithArguments('-dS', 'build')

    then: "the build is up-to-date"
    resultSecondRun.tasks.each {
      if (!(it.path in [":discoverMainScriptsExtensions", ":discoverTestScriptsExtensions"])) {
        assert it.outcome in [UP_TO_DATE, NO_SOURCE]
      }
    }

    where:
    gradleVersionArg << GradleVersion.current()//getSupportedGradleVersions()
  }
}
