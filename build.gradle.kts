plugins {
    kotlin("jvm") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
}

group = "ir.ac.yazd"
version = "1.0-SNAPSHOT"

repositories {
    google()
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    dokkaHtml {
        outputDirectory = "$buildDir/dokka"
    }
}
