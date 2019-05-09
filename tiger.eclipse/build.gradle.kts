plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

bundle {
  requireTargetPlatform("javax.inject")

  requireBundle(":spoofax.eclipse")
  
  requireEmbeddingBundle(":spoofax.eclipse.externaldeps")
  requireEmbeddingBundle(":tiger.eclipse.externaldeps")
}

dependencies {
  // Dependency constraints.
  api(platform(project(":depconstraints")))

  // Compile-time annotations.
  compileOnly("org.checkerframework:checker-qual-android")
}
