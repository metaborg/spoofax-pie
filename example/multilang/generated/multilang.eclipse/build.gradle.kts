plugins {
    id("org.metaborg.coronium.bundle")
}

repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

mavenize {
    majorVersion.set("2022-06")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
    bundleApi(project(":signature.eclipse"))
    bundleApi(project(":module.eclipse"))
    bundleApi(project(":minisdf.eclipse"))
    bundleApi(project(":ministr.eclipse"))
}
