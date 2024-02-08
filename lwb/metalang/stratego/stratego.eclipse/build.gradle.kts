plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.spoofax.compiler.gradle.eclipse")
}

dependencies {
  // Required because @Nullable has runtime retention (which includes classfile retention), and the Java compiler requires access to it.
  compileOnly("com.google.code.findbugs:jsr305")
}

languageEclipseProject {
  adapterProject.set(project(":stratego"))
}

mavenize {
  majorVersion.set("2022-06")
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
    // Export `stratego.build` because `stratego` depends on it. Allow split package because dagger generates classes in
    // the same package
    exportPackages.add("mb.stratego.build.*;-split-package:=merge-first")
    // Embed `stratego.compiler.pack` because `stratego` depends on it
    privatePackages.add("mb.stratego.compiler.pack.*")
    manifest {
      attributes(
        Pair("Export-Package", exportPackages.joinToString(", ")),
        Pair("Private-Package", privatePackages.joinToString(", "))
      )
    }
  }
}
