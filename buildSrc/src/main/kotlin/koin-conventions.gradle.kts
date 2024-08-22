@file:Suppress("PropertyName")

val logback_version: String by rootProject
val koin_version: String by rootProject
val koin_annotations_version: String by rootProject

plugins {
    id("common-conventions")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.insert-koin:koin-annotations:$koin_annotations_version")
    ksp("io.insert-koin:koin-ksp-compiler:$koin_annotations_version")
}
