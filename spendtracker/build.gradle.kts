plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    dependencies {
        implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.1.0")


        testImplementation(kotlin("test"))
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}