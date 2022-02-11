plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

languageEclipseProject {
  adapterProject.set(project(":statix"))
}

tasks {
  "jar"(Jar::class) {
    val exportPackages = LinkedHashSet<String>()
    // Allow split package because `statix.solver` also includes the `mb.statix` package. Add before existing exports so
    // that this takes precedence
    exportPackages.add("mb.statix.*;-split-package:=merge-first")
    val existingExportPackages = manifest.attributes.get("Export-Package")
    if(existingExportPackages != null) {
      exportPackages.add(existingExportPackages.toString())
    }
    val privatePackages = LinkedHashSet<String>()
    val existingPrivatePackages = manifest.attributes.get("Private-Package")
    if(existingPrivatePackages != null) {
      privatePackages.add(existingPrivatePackages.toString())
    }
    manifest {
      attributes(
        Pair("Export-Package", exportPackages.joinToString(", ")),
        Pair("Private-Package", privatePackages.joinToString(", "))
      )
    }
  }
}
