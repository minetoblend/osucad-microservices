plugins {
    id("ktor-conventions")
    id("koin-conventions")
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(project(":infra:microservice-base"))
    implementation(project(":core:gateway"))
    implementation(project(":core:protocol"))

    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("io.github.viartemev:rabbitmq-kotlin:0.7.0-SNAPSHOT")
}
