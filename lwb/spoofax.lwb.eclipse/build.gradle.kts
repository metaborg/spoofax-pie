plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name:$version"

mavenize {
  majorVersion.set("2020-12")
}

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  bundleImplementation(project(":cfg.eclipse"))
  bundleImplementation(project(":esv.eclipse"))
  bundleImplementation(project(":sdf3.eclipse"))
  bundleImplementation(project(":stratego.eclipse"))
  bundleImplementation(project(":statix.eclipse"))

  bundleImplementation(project(":libspoofax2.eclipse"))
  bundleImplementation(project(":libstatix.eclipse"))

  bundleEmbedImplementation(compositeBuild("spoofax.lwb.dynamicloading"))

  bundleTargetPlatformApi(eclipse("javax.inject"))
}
