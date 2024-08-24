plugins {
    id("ktor-conventions")
    id("koin-conventions")
}

dependencies {
    implementation(project(":core:protocol"))
    implementation(project(":core:gateway"))
    implementation(project(":core:delta-orderer"))
    implementation(project(":infra:microservice-base"))
}
