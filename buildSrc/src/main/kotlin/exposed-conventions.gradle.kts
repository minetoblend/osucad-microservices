@file:Suppress("PropertyName")

import org.gradle.kotlin.dsl.provideDelegate


val exposed_version: String by rootProject
val mysql_connector_version: String by rootProject
val hikari_version: String by rootProject
val h2_version: String by rootProject

plugins {
    id("common-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.exposed", "exposed-core", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-dao", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-json", exposed_version)
    implementation("com.h2database", "h2", h2_version)
    implementation("com.mysql", "mysql-connector-j", mysql_connector_version)
    implementation("com.zaxxer", "HikariCP", hikari_version)
}
