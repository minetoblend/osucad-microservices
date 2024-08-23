plugins {
    id("common-conventions")
}

dependencies {
    implementation(project(":core:protocol"))
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger:1.0.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    implementation("dev.inmo:krontab:2.4.0")
}
