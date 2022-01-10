plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

fun compositeBuild(name: String) = "$group:$name:$version"
dependencies {
  bundleImplementation(compositeBuild("strategolib.eclipse"))
}

languageEclipseProject {
  adapterProject.set(project(":gpp"))
  compilerInput {
    languageGroup("mb.spoofax.lwb")
  }
}

tasks {
  "jar"(Jar::class) {
    val exportPackages = LinkedHashSet<String>()
    // Export `strategolib` package.
    exportPackages.add("gpp.*")
    val existingExportPackages = manifest.attributes.get("Export-Package")
    if(existingExportPackages != null) {
      exportPackages.add(existingExportPackages.toString())
    }
    manifest {
      attributes(
        Pair("Export-Package", exportPackages.joinToString(", "))
      )
    }
  }
}
