plugins {
  id("org.metaborg.coronium.repository")
  `maven-publish`
}

fun compositeBuild(name: String) = "$group:$name:$version"

mavenize {
  majorVersion.set("2021-03")
}

repository {
  eclipseInstallationAppName.set("Spoofax3")
  createEclipseInstallationPublications.set(true)
  createEclipseInstallationWithJvmPublications.set(true)
}

dependencies {
  feature(project(":spoofax.lwb.eclipse.feature"))
}
