plugins {
    id("ktor-conventions")
    id("koin-conventions")
}

dependencies {
    implementation(project(":core:gateway"))
    implementation(project(":core:protocol"))
    implementation(project(":infra:microservice-base"))
}
