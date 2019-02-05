package io.ebean.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import spock.lang.Shared
import spock.lang.Specification

import java.lang.management.ManagementFactory

abstract class BaseFuncTest extends Specification {

  @Shared
  File testKitDir

  void setupSpec() {
    // define the location of testkit, taking into account that multiple test workers might run in parallel
    testKitDir = new File("build/testkit").absoluteFile
    def workerNum = System.getProperty("org.gradle.test.worker")
    if (workerNum) {
      testKitDir = new File(testKitDir, workerNum)
    }
  }

  @Rule
  TemporaryFolder tempDir = new TemporaryFolder()

  @Rule
  TestName testName = new TestName()

  File workspaceDir
  GradleVersion gradleVersion

  void setup() {
    workspaceDir = new File(tempDir.root, testName.methodName)
    gradleVersion = determineGradleVersion()
  }

  protected BuildResult runWithArguments(String... args) {
    gradleRunner(args).build()
  }

  protected BuildResult runAndFailWithArguments(String... args) {
    gradleRunner(args).buildAndFail()
  }

  private GradleRunner gradleRunner(String... args) {
    GradleRunner.create()
      .withPluginClasspath()
      .withTestKitDir(testKitDir)
      .withProjectDir(workspaceDir)
      .withArguments(args)
      .forwardOutput()
      .withGradleVersion(gradleVersion.version)
      .withDebug(isDebuggerAttached())
  }

  protected File getBuildFile() {
    file('build.gradle')
  }

  File dir(String path) {
    def file = new File(workspaceDir, path)
    assert file.parentFile.mkdirs() || file.parentFile.directory
    if (file.exists()) {
      assert file.directory
    } else {
      assert file.mkdir()
    }
    file
  }

  protected File file(String path) {
    file(workspaceDir, path)
  }

  protected File file(File dir, String path) {
    def file = new File(dir, path)
    assert file.parentFile.mkdirs() || file.parentFile.directory
    if (file.exists()) {
      assert file.file
    } else {
      assert file.createNewFile()
    }
    file
  }

  protected static boolean isDebuggerAttached() {
    ManagementFactory.runtimeMXBean.inputArguments.toString().indexOf("-agentlib:jdwp") > 0
  }

  protected static GradleVersion determineGradleVersion() {
    def injectedGradleVersionString = System.getProperty('testContext.gradleVersion')
    injectedGradleVersionString ? GradleVersion.version(injectedGradleVersionString) : GradleVersion.current()
  }

  protected List<String> getSupportedGradleVersions() {
    def versions = ["4.9", // Earliest supported version
                    // ... add more versions here when needed
    ].collect { GradleVersion.version(it) }

    // Also try with whatever version we are building with (unless we already have it in the list above)
    return (versions + GradleVersion.current()).unique()
  }

}
