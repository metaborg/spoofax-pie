plugins {
  `java-platform`
  `maven-publish`
}

val logVersion = "0.2.2"
val resourceVersion = "0.3.1"
val pieVersion = "0.5.3"
val spoofaxCoreVersion = "2.6.0-SNAPSHOT"
val daggerVersion = "2.21"

dependencies {
  constraints {
    // Main
    api("org.metaborg:log.api:$logVersion")
    api("org.metaborg:log.backend.noop:$logVersion")
    api("org.metaborg:log.backend.logback:$logVersion")

    api("org.metaborg:resource:$resourceVersion")

    api("org.metaborg:pie.api:$pieVersion")
    api("org.metaborg:pie.runtime:$pieVersion")
    api("org.metaborg:pie.dagger:$pieVersion")

    api("org.metaborg:org.spoofax.terms:$spoofaxCoreVersion")
    api("org.metaborg:org.spoofax.jsglr:$spoofaxCoreVersion")
    api("org.metaborg:org.spoofax.interpreter.core:$spoofaxCoreVersion")
    api("org.metaborg:org.strategoxt.strj:$spoofaxCoreVersion")

    api("com.google.dagger:dagger:$daggerVersion")
    api("com.google.dagger:dagger-compiler:$daggerVersion")
    api("javax.inject:javax.inject:1")

    api("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.

    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.2.0")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    api("com.google.jimfs:jimfs:1.1")
  }
}

publishing {
  publications {
    create<MavenPublication>("JavaPlatform") {
      from(components["javaPlatform"])
    }
  }
}
