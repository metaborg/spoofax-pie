plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.coronium.bundle")
}

fun compositeBuild(name: String) = "$group:$name"

bundle {
  // Currently, target platform dependencies are not re-exported, so we need to depend on all relevant modules from the target platform
  requireTargetPlatform("javax.inject")
  requireTargetPlatform("org.eclipse.core.runtime")
  requireTargetPlatform("org.eclipse.core.expressions")
  requireTargetPlatform("org.eclipse.core.resources")
  requireTargetPlatform("org.eclipse.core.filesystem")
  requireTargetPlatform("org.eclipse.ui")
  requireTargetPlatform("org.eclipse.ui.views")
  requireTargetPlatform("org.eclipse.ui.editors")
  requireTargetPlatform("org.eclipse.ui.console")
  requireTargetPlatform("org.eclipse.ui.workbench")
  requireTargetPlatform("org.eclipse.ui.workbench.texteditor")
  requireTargetPlatform("org.eclipse.ui.ide")
  requireTargetPlatform("org.eclipse.jface.text")
  requireTargetPlatform("org.eclipse.swt")
  requireTargetPlatform("com.ibm.icu")
}

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))

  bundleApi("$group:spoofax.eclipse:$version")
  bundleApi("$group:spoofax.eclipse.externaldeps:$version")
  bundleApi(project(":tiger.eclipse.externaldeps"))

  compileOnly("org.checkerframework:checker-qual-android")

  annotationProcessor("com.google.dagger:dagger-compiler")
}
