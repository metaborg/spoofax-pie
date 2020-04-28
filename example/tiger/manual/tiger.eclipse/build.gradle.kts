plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name"

bundle {
  requireTargetPlatform("javax.inject")

  requireBundleModule(group.toString(), "spoofax.eclipse", version.toString())

  requireEmbeddingBundleModule(group.toString(), "spoofax.eclipse.externaldeps", version.toString())
  requireEmbeddingBundleProject(":tiger.eclipse.externaldeps")
}

dependencies {
  // Dependency constraints.
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  // Compile-time annotations.
  compileOnly("org.checkerframework:checker-qual-android")

  // Annotation processors.
  annotationProcessor("com.google.dagger:dagger-compiler")
}
