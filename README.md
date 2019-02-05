# Ebean Gradle Plugin
This plugin performs Ebean enhancement (entity, transactional, query bean) on Java, Groovy, Scala or Kotlin classes.
It can optionally generate query beans from entity beans written in Java or Kotlin.

## Usage
Add the "io.ebean" plugin to your build.
See https://plugins.gradle.org/plugin/io.ebean for details on this.

You can use the configuration block "ebean" like this:

```gradle
ebean {
  debugLevel = 0 // Ebean enhancer debug level. Can be from 0-9.
  boolean queryBeans = false // When true registers Java querybean generation.
  kotlin = false // TODO: Still needed?
  generatorVersion = '11.27.1' // querybean-generator version for use when generating query beans.
  querybeanVersion = "11.30.1" // ebean-querybean version for use when generating query beans.
  configurationFile = file("src/main/resources/ebean.mf") // Location of the EBean configuration file (if present).
}
```

Refer to http://ebean-orm.github.io/docs/tooling/gradle for additional documentation.

If you need help, you can post questions or issues to the [Ebean google group](https://groups.google.com/forum/#!forum/ebean).

## Limitations
### Gradle version requirement
The plugin requires Gradle version 4.9 or later.

If you need support for older versions of Gradle, you may try version 11.26.1 of this plugin.
Note, however, that this version does not support incremental compilation, lazy initialization, the Gradle build cache, up-to-date checking and more.

### Joint compilation with Java and Kotlin
Joint compilation is when you have Java and Kotlin code mixed in the same project, either in separate folders (like src/main/java and src/main/kotlin) or the same folder.
This allows you to use Java code in your Kotlin classes and vice versa.
The Kotlin plugin achieves this by compiling Kotlin first, with stubs for any Java class references, and then compiling the Java classes after.
However, this is a problem for the Ebean Gradle plugin, as enhancing the classes require all of them to have been compiled first.
This means that you cannot have a Kotlin entity class that inherits from a Java class in the same project.
The reverse is possible though.
 
As a workaround for this, consider if it is possible to rewrite the Java superclasses as Kotlin classes.
If not, you should be able to extract the Java source code into a separate Gradle project, and let your Kotlin project depend on it.
This way you can use Java superclasses in your Kotlin entities. 

### Query bean generation
Query bean generation is only supported on Java and Kotlin classes - not Scala or Groovy classes.

## Examples
See full examples in the [src/integration-test](src/integration-test) folder.
Additional examples are found in [ebean-orm-examples](https://github.com/ebean-orm-examples) organization.

### Example build.gradle (Kotlin)
// TODO: Revisit this example, and consider writing it in Kotlin instead
```gradle
group 'org.example'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '1.2.0'
    ext.ebean_version = "11.24.1"
    ext.postgresql_driver_version = "9.4.1207.jre7"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "io.ebean:ebean-gradle-plugin:11.2.1"
    }
}

apply plugin: 'kotlin'
apply plugin: 'io.ebean'

repositories {
    mavenLocal()
    mavenCentral()
}

sourceSets {
    main.java.srcDirs += [file("$buildDir/generated/source/kapt/main")]
}

dependencies {

    compile "org.postgresql:postgresql:$postgresql_driver_version"
    compile "io.ebean:ebean:$ebean_version"
    compile "io.ebean:ebean-querybean:11.24.1"

    compile "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"

    testCompile "org.avaje.composite:junit:1.1"
}

kapt {
    generateStubs = true
}

ebean {
    debugLevel = 1 //1 - 9
}

test {
    useTestNG()
    testLogging.showStandardStreams = true
    testLogging.exceptionFormat = 'full'
}
```

### Example build.gradle (Java)
```gradle
plugins {
  id 'groovy'
  id 'io.ebean'
}

repositories {
  jcenter()
}

group 'org.example'
version '1.0-SNAPSHOT'

dependencies {
  implementation localGroovy()
  implementation group: 'io.ebean', name: 'ebean', version: '11.26.1'
}

ebean {
  debugLevel = 9 // 1 - 9
  queryBeans = true
}
```

##Plugin Development
### IDE
If you use IntelliJ, the best way to open the project is to import the file `src/integration-test/build.gradle`.
This way you will have not only the integration test available, but also the plugin itself in the same IntelliJ project.

### Building the plugin
Simply invoke `gradlew build` on the root project folder.
This will build the plugin and run the integration test with various versions of Gradle.

For prototyping changes in the plugin in the development phase, you may find it easier to run `gradlew build` from within one of the subprojects in the `integration-test` folder.
This will run the integration test for a particular language with version of Gradle used by the wrapper.
It builds any changes to the plugin automatically.
This is much faster than the full integration test suite, but obviously you need to run this at some point to ensure everything still works.
