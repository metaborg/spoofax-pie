plugins {
    `java-platform`
    `maven-publish`
}


dependencies {
    constraints {
        // Own projects
        api(project(":spoofax.common"))
        api(project(":aterm.common"))
        api(project(":jsglr.common"))
        api(project(":jsglr1.common"))
        api(project(":jsglr.pie"))
        api(project(":jsglr2.common"))
        api(project(":esv.common"))
        api(project(":stratego.common"))
        api(project(":constraint.common"))
        api(project(":nabl2.common"))
        api(project(":statix.codecompletion"))
        api(project(":statix.codecompletion.pie"))
        api(project(":statix.common"))
        api(project(":statix.pie"))
        api(project(":statix.multilang"))
        api(project(":spoofax2.common"))
        api(project(":tego.runtime"))
        api(project(":tooling.eclipsebundle"))  // TODO: bundle versions are not picked up when consuming this platform?

        api(project(":spoofax.compiler.interfaces"))
        api(project(":spoofax.resource"))
        api(project(":spoofax.core"))
        api(project(":spoofax.cli"))
        api(project(":spoofax.intellij"))
        api(project(":spoofax.eclipse")) // TODO: bundle versions are not picked up when consuming this platform?
        api(project(":spoofax.compiler"))
        api(project(":spoofax.compiler.gradle"))
        api(project(":spoofax.compiler.eclipsebundle"))  // TODO: bundle versions are not picked up when consuming this platform?


        // Main dependencies
        /// Common
        api(libs.metaborg.common)
        /// Log
        api(libs.metaborg.log.api)
        api(libs.metaborg.log.backend.logback)
        api(libs.metaborg.log.backend.slf4j)
        api(libs.metaborg.log.dagger)
        /// SLF4j
        api(libs.slf4j.simple)
        /// Resource
        api(libs.metaborg.resource.api)
        api(libs.metaborg.resource.dagger)
        /// PIE
        api(libs.metaborg.pie.api)
        api(libs.metaborg.pie.graph)
        api(libs.metaborg.pie.runtime)
        api(libs.metaborg.pie.dagger)
        api(libs.metaborg.pie.task.java)
        api(libs.metaborg.pie.task.archive)
        api(libs.metaborg.pie.serde.fst)
        /// Spoofax 2.x
        runtime(libs.strategoxt.minjar)
        /// Spoofax 2.x with devenv override
        api(libs.strategoxt.strj)
        api(libs.spoofax.terms)
        api(libs.metaborg.util)
        api(libs.interpreter.core)
        api(libs.jsglr)
        api(libs.jsglr2)
        api(libs.jsglr.shared)
        api(libs.sdf2table)
        api(libs.sdf2parenthesize)
        api(libs.parsetable)
        api(libs.stratego.build)
        api(libs.nabl2.terms)
        api(libs.nabl2.solver)
        api(libs.statix.solver)
        api(libs.statix.generator)

        /// Picocli
        api(libs.picocli)
        api(libs.picocli.codegen)

        // Annotation-only dependencies
        /// javax.inject
        api(libs.javax.inject)
        /// Checkerframework
        api(libs.checkerframework.android) // Use android version: annotation retention policy is class instead of runtime.
        /// FindBugs JSR305
        api(libs.jsr305)

        // Annotation processor dependencies
        /// Dagger
        api(libs.dagger)
        api(libs.dagger.compiler)
        /// Derive4j
        api(libs.derive4j)
        api(libs.derive4j.annotation)
        /// org.immutables
        api(libs.immutables.serial)
        api(libs.immutables.value)
        api(libs.immutables.value.annotations)
        // Yaml
        api(libs.snakeyaml)

        // Gradle plugins
        api(libs.spoofax3.gradle)


        // Test dependencies // TODO: should be in a separate platform?
        api(libs.junit.api)
        api(libs.mockito.kotlin)
        api(libs.jimfs)
        api(libs.equalsverifier)
    }
}

publishing {
    publications {
        create<MavenPublication>("JavaPlatform") {
            from(components["javaPlatform"])
        }
    }
}
