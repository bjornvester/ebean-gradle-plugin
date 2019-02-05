package io.ebean.gradle

import groovy.io.FileType
import groovy.transform.CompileStatic
import io.ebean.enhance.Transformer
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.nio.file.Path

@CompileStatic
class EbeanEnhancer implements Closeable {

  private final Logger logger = Logging.getLogger(EbeanEnhancer.class)

  /**
   * Directory containing .class files.
   */
  private final Path outputDir

  private final Transformer combinedTransform

  private final URLClassLoader classLoader

  EbeanEnhancer(Path outputDir, URL[] extraClassPath, ClassLoader contextLoader, EnhancePluginExtension pluginExtension) {
    logger.info("Creating Ebean enhancer for directory '$outputDir' and classpath $extraClassPath")
    this.outputDir = outputDir
    this.classLoader = new URLClassLoader(extraClassPath, contextLoader)

    String args = "debug=" + pluginExtension.debugLevel
    this.combinedTransform = new Transformer(classLoader, args)
  }

  void enhance() {
    outputDir.eachFileRecurse(FileType.FILES) { path ->
      if (path.fileName.toString().endsWith(".class")) {
        String className = makeClassName(outputDir, path)
        if (!isIgnorableClass(className)) {
          enhanceClassFile(path, className)
        }
      }
    }
  }

  @Override
  void close() {
    DefaultGroovyMethodsSupport.closeWithWarning(classLoader)
  }

  private void enhanceClassFile(Path classFile, String className) {
    byte[] classBytes

    try {
      classBytes = classFile.bytes
    } catch (any) {
      throw new EnhanceException("Unable to read class file ${classFile.fileName.toString()} for enhancement", any)
    }

    String jvmClassName = className.replace('.', '/')
    byte[] classBytesEnhanced

    try {
      classBytesEnhanced = combinedTransform.transform(classLoader, jvmClassName, null, null, classBytes)
    } catch (any) {
      throw new EnhanceException("Unable to parse class file $jvmClassName for enhancement", any)
    }

    if (classBytesEnhanced) {
      try {
        classFile.bytes = classBytesEnhanced
      } catch (any) {
        throw new EnhanceException("Unable to store enhanced class data back to file $jvmClassName", any)
      }

      logger.debug("Enhanced $jvmClassName")
    }
  }

  /**
   * Ignore scala lambda anonymous function and groovy meta info classes & closures.
   */
  private static boolean isIgnorableClass(String className) {
    return className.contains('$$anonfun$') || className.contains('$_')
  }

  /**
   * Returns the fully qualified class name given the base path and file path.
   */
  private static String makeClassName(Path basePath, Path classFile) {
    Path classRelPath = basePath.relativize(classFile)
    classRelPath.toString().replaceAll('[.]class$', '').replace('\\', '.').replace('/', '.')
  }
}
