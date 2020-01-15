import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    kotlin("plugin.jpa") version "1.3.61"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.expediagroup:graphql-kotlin-spring-server:1.4.2")
    implementation("org.hibernate:hibernate-core:5.4.2.Final")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.security:spring-security-test")
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-core
//    implementation("org.springframework.security:spring-security-core:5.2.1.RELEASE")
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-web
//    implementation("org.springframework.security:spring-security-web:5.2.1.RELEASE")


    implementation("com.auth0:java-jwt:3.9.0")
//    implementation("org.springframework.boot:spring-boot-starter-web")
    // https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api
    implementation("javax.servlet:javax.servlet-api:4.0.1")



    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.hibernate:hibernate-testing:5.4.2.Final")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
