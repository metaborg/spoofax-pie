plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

languageEclipseProject {
  adapterProject.set(project(":sdf3"))
}

tasks {
  "jar"(Jar::class) {
    val exportPackages = LinkedHashSet<String>()
    val existingExportPackages = manifest.attributes.get("Export-Package")
    if(existingExportPackages != null) {
      exportPackages.add(existingExportPackages.toString())
    }
    val privatePackages = LinkedHashSet<String>()
    val existingPrivatePackages = manifest.attributes.get("Private-Package")
    if(existingPrivatePackages != null) {
      privatePackages.add(existingPrivatePackages.toString())
    }
    privatePackages.add("org.metaborg.sdf2parenthesize.*") // Embed `sdf2parenthesize` because `sdf3` depends on it.
    manifest {
      attributes(
        Pair("Export-Package", exportPackages.joinToString(", ")),
        Pair("Private-Package", privatePackages.joinToString(", "))
      )
    }
  }
}

mavenize {
  majorVersion.set("2022-06")
}
