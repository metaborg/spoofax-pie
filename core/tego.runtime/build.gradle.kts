plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    jacoco
}

dependencies {
    api(platform(project(":spoofax.depconstraints")))
    annotationProcessor(platform(project(":spoofax.depconstraints")))
    testAnnotationProcessor(platform(project(":spoofax.depconstraints")))

    api(libs.metaborg.common)
    api(libs.metaborg.log.api)

    api(libs.metaborg.log.dagger)
    api(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.checkerframework.android)

    testCompileOnly(libs.checkerframework.android)
    testImplementation(libs.metaborg.log.backend.slf4j)
    testImplementation(libs.slf4j.simple)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}
