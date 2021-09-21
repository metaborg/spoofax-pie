plugins {
  id("org.metaborg.coronium.feature")
}

mavenize {
  majorVersion.set("2021-03")
}

dependencies {
  bundle(project(":spoofax.lwb.eclipse"))
}

tasks {
  withType<mb.coronium.task.EclipseRun> {
    jvmArgs("-Xss16M") // Set required stack size, mainly for serialization.
  }
}
