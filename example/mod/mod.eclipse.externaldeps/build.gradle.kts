plugins {
  id("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps")
}

spoofaxEclipseExternaldepsProject {
  adapterProject.set(project(":mod.spoofax"))
}
