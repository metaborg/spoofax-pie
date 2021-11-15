plugins {
  id("org.metaborg.coronium.feature")
}

mavenize {
  majorVersion.set("2021-03")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  bundle(project(":spoofax.lwb.eclipse"))
  bundle(project(":rv32im.eclipse"))
}

tasks {
  withType<mb.coronium.task.EclipseRun> {
    jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
  }
}
