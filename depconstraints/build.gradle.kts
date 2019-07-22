plugins {
  `java-platform`
  `maven-publish`
}

val logVersion = "develop-SNAPSHOT"
val resourceVersion = "develop-SNAPSHOT"
val pieVersion = "develop-SNAPSHOT"
val spoofaxCoreVersion = "2.6.0-SNAPSHOT"
val daggerVersion = "2.21"
val derive4jVersion = "1.1.1"

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
    runtime("org.metaborg:strategoxt-min-jar:$spoofaxCoreVersion")
    api("org.metaborg:nabl2.solver:$spoofaxCoreVersion")
    api("org.metaborg:nabl2.terms:$spoofaxCoreVersion")
    api("org.metaborg:statix.solver:$spoofaxCoreVersion")

    api("com.google.dagger:dagger:$daggerVersion")
    api("com.google.dagger:dagger-compiler:$daggerVersion")
    api("org.derive4j:derive4j:$derive4jVersion")
    api("org.derive4j:derive4j-annotation:$derive4jVersion")
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
