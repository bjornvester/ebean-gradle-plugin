plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.11"
  id("org.jetbrains.kotlin.kapt") version "1.3.11"
  id("io.ebean")
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(project(":test-java"))
  implementation("io.ebean:ebean:11.30.1")
  implementation("io.ebean:ebean-querybean:11.27.1")

  kapt("io.ebean:kotlin-querybean-generator:11.27.1")
  kaptTest("io.ebean:kotlin-querybean-generator:11.27.1")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
  testRuntimeOnly("org.slf4j:slf4j-simple:1.7.25")
  testRuntimeOnly("com.h2database:h2:1.4.197")
}

repositories {
  jcenter()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    setExceptionFormat("full")
    setShowStandardStreams(true)
    //events("passed", "skipped", "failed")
  }
}
