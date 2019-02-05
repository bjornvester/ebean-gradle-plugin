package io.ebean.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.*

class ScalaFuncTest extends BaseFuncTest {

  @Unroll
  def "The plugin can be applied to a Scala project for Gradle version #gradleVersionArg"() {
    given: "the Gradle version under test"
    gradleVersion = gradleVersionArg

    and: "a main entity class and a test entity class in Java"
    file("src/main/java/sample/MyJavaEntity.java") << getClass().getResource('/java/MyJavaEntity.java').text
    file("src/test/java/sample/MyJavaTestEntity.java") << getClass().getResource('/java/MyJavaTestEntity.java').text

    and: "a main entity class and a test entity class in Scala"
    file("src/main/scala/sample/MyScalaEntity.scala") << getClass().getResource('/scala/MyScalaEntity.scala').text
    file("src/test/scala/sample/MyScalaTestEntity.scala") << getClass().getResource('/scala/MyScalaTestEntity.scala').text

    and: "an ebean.mf configuration file"
    file("src/main/resources/ebean.mf") << getClass().getResource('/ebean.mf').text

    and: "a Gradle build file, applying the plugin to a Scala build of the project"
    String buildFileContent = getClass().getResource('/scala/build.gradle').text

    buildFileContent = buildFileContent.replace("queryBeans = true", "queryBeans = false")

    buildFile << buildFileContent

    when: "running the build"
    def result = runWithArguments('-dS', 'build')

    then: "the build was successful"
    result.task(":build").outcome == SUCCESS

    and: "all four classes for Java, Scala, Main and Test were enhanced"
    result.output.contains('Enhanced sample/MyJavaEntity')
    result.output.contains('Enhanced sample/MyJavaTestEntity')
    result.output.contains('Enhanced sample/MyScalaEntity')
    result.output.contains('Enhanced sample/MyScalaTestEntity')

    and: "query beans were generated for both the MyEntity and MyTestEntity Java classes"
    // Query generation for Scala classes is not supported
    result.output.contains('Enhanced sample/query/QMyJavaEntity$Alias')
    result.output.contains('Enhanced sample/query/QMyJavaTestEntity$Alias')

    when: "running the build a second time without changes"
    BuildResult resultSecondRun = runWithArguments('-dS', 'build')

    then: "the build is up-to-date"
    resultSecondRun.tasks.each {
      assert it.outcome in [UP_TO_DATE, NO_SOURCE]
    }

    where:
    gradleVersionArg << GradleVersion.current()//getSupportedGradleVersions()
  }
}
