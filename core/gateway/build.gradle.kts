plugins {
    id("common-conventions")
}

dependencies {
    implementation(project(":core:protocol"))

    implementation("io.micrometer:micrometer-core:1.12.4")
}
