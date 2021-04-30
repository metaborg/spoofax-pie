plugins {
  id("org.metaborg.coronium.feature")
}

mavenize {
  majorVersion.set("2020-12")
}

dependencies {
  bundle(project(":spoofax.lwb.eclipse"))
}
