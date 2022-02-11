plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  adapterProject.set(project(":strategolib"))
}

tasks {
  "jar"(Jar::class) {
    val exportPackages = LinkedHashSet<String>()
    // Export `strategolib` package.
    exportPackages.add("strategolib.*")
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
