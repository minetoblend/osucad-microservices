plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "osucad-microservices"

include(
    ":core:common",
    ":core:protocol",
    ":core:gateway",
    ":core:delta-orderer",
    ":infra:microservice-base",
    ":infra:gateway",
    ":devserver"
)
