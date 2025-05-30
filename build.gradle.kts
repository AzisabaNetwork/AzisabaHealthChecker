plugins {
    id("com.gradleup.shadow") version "8.3.6"
    java
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.4")
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.1")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("net.sf.jopt-simple:jopt-simple:6.0-alpha-3")
    implementation("org.json:json:20250107")
    implementation("net.blueberrymc:native-util:2.2.0")
    compileOnly("org.jetbrains:annotations:23.0.0")
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }

    getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        manifest {
            attributes(
                "Main-Class" to "net.azisaba.healthchecker.Main",
                "Multi-Release" to true,
            )
        }
        archiveFileName.set("AzisabaHealthChecker.jar")
    }
}
