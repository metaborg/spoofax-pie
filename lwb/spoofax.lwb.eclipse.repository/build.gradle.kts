plugins {
  id("org.metaborg.coronium.repository")
}

fun compositeBuild(name: String) = "$group:$name:$version"

mavenize {
  majorVersion.set("2020-12")
}

dependencies {
  feature(project(":spoofax.lwb.eclipse.feature"))
}
