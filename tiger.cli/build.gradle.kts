plugins {
  id("org.metaborg.gradle.config.java-application")
}

application {
  mainClassName = "mb.tiger.cmd.Main"
}

// TODO: enable when tiger.cmd works with transforms.
//tasks.getByName<JavaExec>("run").args = listOf("parse", "../org.metaborg.lang.tiger/example/xmpl2/matrix_with_errors.tig")
//tasks.getByName<JavaExec>("run").args = listOf("parse-string", "1 + 1", "parse-string", "1 + 2")

dependencies {
  implementation(platform(project(":depconstraints")))

  implementation(project(":tiger.spoofax"))
  implementation(project(":spoofax.cli"))
  implementation("org.metaborg:log.backend.noop")
  implementation("org.metaborg:pie.runtime")
  implementation("org.metaborg:pie.dagger")

  compileOnly("org.checkerframework:checker-qual-android")
}
