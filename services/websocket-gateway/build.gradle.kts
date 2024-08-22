plugins {
    id("ktor-conventions")
    id("koin-conventions")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.hayden:khealth:2.1.1")
    implementation("io.micrometer:micrometer-registry-datadog:1.13.3")
}
