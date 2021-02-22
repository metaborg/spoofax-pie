plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.jetbrains.intellij")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  implementation(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  implementation("org.metaborg:pie.runtime")
  implementation(compositeBuild("spoofax.core"))
  implementation(compositeBuild("spoofax.intellij"))
  implementation(project(":tiger.spoofax")) {
    exclude(group = "org.slf4j")
  }

  implementation("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}

intellij {
  version = "2020.2.4" // 2020.2.4 is the last version that can be built with Java 8.
  instrumentCode = false // Skip non-incremental and slow code instrumentation.
}

tasks {
  named("buildSearchableOptions") {
    enabled = false // Skip non-incremental and slow `buildSearchableOptions` task from `org.jetbrains.intellij`.
  }

  named<org.jetbrains.intellij.tasks.RunIdeTask>("runIde") {
    this.jbrVersion("11_0_2b159") // Set JBR version because the latest one cannot be downloaded.
    // HACK: make task depend on the runtime classpath to forcefully make it depend on `spoofax.intellij`, which the
    //       `org.jetbrains.intellij` plugin seems to ignore. This is probably because `spoofax.intellij` is a plugin
    //       but is not listed as a plugin dependency. This hack may not work when publishing this plugin.
    dependsOn(configurations.runtimeClasspath)
  }
}
