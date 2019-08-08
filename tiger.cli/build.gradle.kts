plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.tiger.cli.Main"
}

// TODO: enable when tiger.cmd works with transforms.
tasks.getByName<JavaExec>("run").args = listOf("parse-file", "../org.metaborg.lang.tiger/example/xmpl2/matrix_with_errors.tig")
//tasks.getByName<JavaExec>("run").args = listOf("parse-string", "1 + 1", "parse-string", "1 + 2")

dependencies {
  implementation(platform(project(":depconstraints")))

  implementation(project(":tiger.spoofax"))
  implementation(project(":spoofax.cli"))
  implementation("org.metaborg:log.backend.slf4j")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.dagger")

  implementation("org.slf4j:slf4j-simple:1.7.26")

  compileOnly("org.checkerframework:checker-qual-android")
}
