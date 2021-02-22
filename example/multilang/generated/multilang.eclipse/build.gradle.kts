plugins {
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))

  bundleApi(project(":signature.eclipse"))
  bundleApi(project(":module.eclipse"))
  bundleApi(project(":minisdf.eclipse"))
  bundleApi(project(":ministr.eclipse"))
}
