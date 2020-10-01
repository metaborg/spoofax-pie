plugins {
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  bundleApi(project(":minisdf.eclipse"))
  bundleApi(project(":ministr.eclipse"))
}
