import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

group = "red.tetracube.iot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val picoCliVersion = "4.6.3"
    implementation("info.picocli:picocli:$picoCliVersion")

    val kubernetesClientVersion = "15.0.1"
    implementation("io.kubernetes:client-java:$kubernetesClientVersion")

    val guavaVersion = "31.1-jre"
    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}