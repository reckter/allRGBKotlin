import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application

    id("org.openjfx.javafxplugin") version "0.0.8"
}

group = "me.reckter"
version = "1.0-SNAPSHOT"

javafx {
    version = "11.0.2"
    modules = listOf("javafx.controls", "javafx.graphics")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("me.tongfei:progressbar:0.9.3")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


application {
    mainClass.set("MainKt")
}
