plugins {
  `java-platform`
  `maven-publish`
}

val logVersion = "0.3.0"
val slf4jVersion = "1.7.30"
val resourceVersion = "0.7.1"
val pieVersion = "0.9.0"
val spoofax2Version = "2.5.8"
val picocliVersion = "4.0.4"

val javaxInjectVersion = "1"
val checkerframeworkVersion = "3.0.0"

val daggerVersion = "2.25.2"
val derive4jVersion = "1.1.1"
val immutablesVersion = "2.8.2"

dependencies {
  constraints {
    // Main
    /// Log
    api("org.metaborg:log.api:$logVersion")
    api("org.metaborg:log.backend.noop:$logVersion")
    api("org.metaborg:log.backend.logback:$logVersion")
    api("org.metaborg:log.backend.slf4j:$logVersion")
    /// SLF4j
    api("org.slf4j:slf4j-simple:$slf4jVersion")
    /// Resource
    api("org.metaborg:resource:$resourceVersion")
    /// PIE
    api("org.metaborg:pie.api:$pieVersion")
    api("org.metaborg:pie.runtime:$pieVersion")
    api("org.metaborg:pie.dagger:$pieVersion")
    /// Spoofax 2
    api("org.metaborg:org.spoofax.terms:$spoofax2Version")
    api("org.metaborg:org.spoofax.jsglr:$spoofax2Version")
    api("org.metaborg:sdf2table:$spoofax2Version")
    api("org.metaborg:org.metaborg.parsetable:$spoofax2Version")
    api("org.metaborg:sdf2parenthesize:$spoofax2Version")
    api("org.metaborg:org.spoofax.interpreter.core:$spoofax2Version")
    api("org.metaborg:org.strategoxt.strj:$spoofax2Version")
    api("org.metaborg:nabl2.solver:$spoofax2Version")
    api("org.metaborg:nabl2.terms:$spoofax2Version")
    api("org.metaborg:statix.solver:$spoofax2Version")
    runtime("org.metaborg:strategoxt-min-jar:$spoofax2Version")
    /// Picocli
    api("info.picocli:picocli:$picocliVersion")
    api("info.picocli:picocli-codegen:$picocliVersion")

    // Annotations only
    /// javax.inject
    api("javax.inject:javax.inject:$javaxInjectVersion")
    /// Checkerframework
    api("org.checkerframework:checker-qual-android:$checkerframeworkVersion") // Use android version: annotation retention policy is class instead of runtime.

    // Annotation processors
    /// Dagger
    api("com.google.dagger:dagger:$daggerVersion")
    api("com.google.dagger:dagger-compiler:$daggerVersion")
    /// Derive4j
    api("org.derive4j:derive4j:$derive4jVersion")
    api("org.derive4j:derive4j-annotation:$derive4jVersion")
    /// org.immutables
    api("org.immutables:value:$immutablesVersion")
    api("org.immutables:value-annotations:$immutablesVersion")


    // Test
    api("org.junit.jupiter:junit-jupiter-api:5.2.0")
    api("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    api("com.google.jimfs:jimfs:1.1")
    api("nl.jqno.equalsverifier:equalsverifier:3.1.12")
  }
}

publishing {
  publications {
    create<MavenPublication>("JavaPlatform") {
      from(components["javaPlatform"])
    }
  }
}
