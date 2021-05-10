plugins {
  id("org.metaborg.coronium.feature")
}

mavenize {
  majorVersion.set("2021-03")
}

dependencies {
  bundle(project(":spoofax.lwb.eclipse"))
}
