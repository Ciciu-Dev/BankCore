plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "me.alex"
version = "0.2.0"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")
    }
}
