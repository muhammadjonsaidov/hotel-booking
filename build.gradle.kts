plugins {
    id("java")
    application
    // P07: Spring Boot builds and runs the web app (./gradlew bootRun).
    id("org.springframework.boot") version "4.1.2"
}

application {
    mainClass = "hotelbooking.Main"
}

group = "uz.pdp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot chooses matching versions for the web, database (P07), Redis (P08) and test libraries.
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("com.tngtech.archunit:archunit-junit6:1.5.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
