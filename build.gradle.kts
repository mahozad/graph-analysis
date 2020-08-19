plugins {
    kotlin("jvm") version "1.4.0"
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
}
