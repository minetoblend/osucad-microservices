plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.0.10")
    implementation("io.ktor.plugin:plugin:2.3.12")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.10-1.0.24")
}
