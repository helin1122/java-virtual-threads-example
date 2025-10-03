import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    java
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val mainSourceSet = the<SourceSetContainer>().named("main")

tasks.register<JavaExec>("runSemaphoreLimitedConcurrency") {
    group = "virtual thread demos"
    description = "Runs the semaphore-limited virtual thread sample."
    mainClass.set("com.example.virtualthreads.semaphore.SemaphoreLimitedConcurrencyDemo")
    classpath = mainSourceSet.get().runtimeClasspath
    // Virtual threads are a preview feature in Java 21, but no extra flags required here.
}
