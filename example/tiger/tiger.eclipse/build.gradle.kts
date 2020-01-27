plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

bundle {
  requireTargetPlatform("javax.inject")

  requireBundle("$group", "spoofax.eclipse", "$version")

  requireEmbeddingBundle("$group", "spoofax.eclipse.externaldeps", "$version")
  requireEmbeddingBundle(":tiger.eclipse.externaldeps")
}

dependencies {
  // Dependency constraints.
  api(platform("$group:spoofax.depconstraints:$version"))
  annotationProcessor(platform("$group:spoofax.depconstraints:$version"))

  // Compile-time annotations.
  compileOnly("org.checkerframework:checker-qual-android")

  // Annotation processors.
  annotationProcessor("com.google.dagger:dagger-compiler")
}
