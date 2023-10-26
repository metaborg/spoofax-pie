plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))


  api(compositeBuild("spoofax.core"))
  api(compositeBuild("spoofax.compiler"))
  api("org.metaborg:common")
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.task.archive")
  api("org.metaborg:pie.task.java")

  // TODO: should the meta-languages use implementation configuration? We don't expose their API AFAICS.
  api(project(":cfg"))
  api(project(":sdf3"))
  api(project(":stratego"))
  api(project(":esv"))
  api(project(":statix"))
  api(project(":dynamix"))
  api(project(":tim"))
  api(project(":llvm"))
  api(project(":sdf3_ext_statix"))
  api(project(":sdf3_ext_dynamix"))

  api(project(":strategolib"))
  api(project(":gpp"))
  api(project(":libspoofax2"))
  api(project(":libstatix"))

  // Using api configuration to make these annotations and processors available to javac that we call during
  // compilation, and to users of this library as well.
  api("org.checkerframework:checker-qual-android")
  api("com.google.dagger:dagger-compiler")


  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")
  annotationProcessor("com.google.dagger:dagger-compiler")


  testImplementation("org.junit.jupiter:junit-jupiter-params:${metaborg.junitVersion}")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:pie.serde.fst")
  testCompileOnly("org.checkerframework:checker-qual-android")
}

tasks.test {
  enableAssertions = false // HACK: disable assertions until we support JSGLR2 parsing for Stratego
  // Show standard err in tests.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
  }
  jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
}
