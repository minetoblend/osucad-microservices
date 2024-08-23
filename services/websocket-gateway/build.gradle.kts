plugins {
    id("ktor-conventions")
    id("koin-conventions")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(project(":core:protocol"))

    implementation("dev.hayden:khealth:2.1.1")
    implementation("io.micrometer:micrometer-registry-datadog:1.13.3")
    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("io.github.viartemev:rabbitmq-kotlin:0.7.0-SNAPSHOT")
}
