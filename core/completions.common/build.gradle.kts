plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":common"))

  compileOnly("org.checkerframework:checker-qual-android")

  testImplementation(platform(project(":spoofax.depconstraints")))
  testCompileOnly("org.checkerframework:checker-qual-android")
  testImplementation("nl.jqno.equalsverifier:equalsverifier")

}
