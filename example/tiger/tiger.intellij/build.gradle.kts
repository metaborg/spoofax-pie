plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.jetbrains.intellij")
}

dependencies {
  implementation(platform("$group:spoofax.depconstraints:$version"))
  annotationProcessor(platform("$group:spoofax.depconstraints:$version"))

  implementation("$group:spoofax.core:$version")
  implementation("$group:spoofax.intellij:$version")
  implementation(project(":tiger.spoofax")) {
    exclude(group = "org.slf4j")
  }

  implementation("com.google.dagger:dagger")

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}

intellij {
  version = "2019.3.2"
}

/*
Explicitly make the `runIde` task depend on creating the JAR of `spoofax.intellij`, because the `org.jetbrains.intellij`
plugin for some reason does not make (or even remove) this dependency, which causes `spoofax.intellij` to not be
recompiled, resulting in all kinds of runtime errors.

My hunch is that this happens because `spoofax.intellij` uses the `org.jetbrains.intellij` plugin to get access to the
IntelliJ API, but is not actually a real plugin and thus we depend on it with a regular Gradle dependency.

We tried getting access to the IntelliJ API via a normal Gradle dependency, but this turns out to the hard because it is
not possible to directly depend on the IntelliJ API. You would need to download a ZIP file, extract it, and add that
directory as an Ivy repository to get access to the IntelliJ API.
*/
// TODO: does not work anymore as composite build, how do we do this now? Is it still needed?
//tasks.getByName("runIde").dependsOn(tasks.getByPath(":spoofax.intellij:jar"))

// Use Java 8 version of JBR (JetBrains Runtime) to run the IDE.
tasks.getByName<org.jetbrains.intellij.tasks.RunIdeTask>("runIde") {
  this.jbrVersion("8u232b1638.6")
}

// Skip non-incremental, slow, and unnecessary buildSearchableOptions task from IntelliJ.
tasks.getByName("buildSearchableOptions").onlyIf { false }
