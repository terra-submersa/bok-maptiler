plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ch.bok"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    flatDir {
        dirs("libs")
    }
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
    maven { url = uri("https://repo.osgeo.org/repository/snapshot/") }
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.geotools:gt-main:28.0")
    implementation("org.geotools:gt-coverage:28.0")
    implementation("org.geotools:gt-geotiff:28.0")
    implementation("org.geotools:gt-epsg-hsql:28.0")
    implementation("org.geotools:gt-referencing:28.0")
    implementation("it.geosolutions.jaiext.affine:jt-affine:1.1.26")
    implementation("it.geosolutions.jaiext.shadedrelief:jt-shadedrelief:1.1.26")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
