plugins {
    id("org.metaborg.gradle.config.java-application")
    id("com.palantir.graal")
}

application {
    mainClass.set("mb.tiger.cli.Main")
}

graal {
    graalVersion("19.2.0.1")

    mainClass("mb.tiger.cli.Main")
    outputName("tiger")
    option("--no-fallback")
    option("--report-unsupported-elements-at-runtime")
    option("--initialize-at-build-time=mb,com,org.metaborg.util.log,org.slf4j,dagger,dagger.internal.InstanceFactory,picocli")
    option("-H:+ReportExceptionStackTraces")
    option("-H:IncludeResources=mb/tiger/target/.*") /* https://github.com/oracle/graal/blob/master/substratevm/RESOURCES.md */
    option("-H:DynamicProxyConfigurationFiles=src/main/gni/proxy.json") /* https://github.com/oracle/graal/blob/master/substratevm/DYNAMIC_PROXY.md */
    option("-H:ReflectionConfigurationFiles=src/main/gni/reflection.json") /* https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md */
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    implementation(platform(libs.metaborg.platform))

    implementation(project(":tiger.spoofax"))
    implementation(libs.spoofax3.cli)
    implementation(libs.metaborg.log.backend.slf4j)
    implementation(libs.metaborg.pie.runtime)
    implementation(libs.metaborg.pie.dagger)

    implementation(libs.slf4j.simple)

    compileOnly(libs.checkerframework.android)

    annotationProcessor(libs.dagger.compiler)
    annotationProcessor(libs.picocli.codegen)
}
