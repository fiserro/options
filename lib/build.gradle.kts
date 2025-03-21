plugins {
    `java-library`
}

group = "io.github.fiserro"

repositories {
    mavenCentral()
    maven { url = uri("https://repo1.maven.org/maven2/") }
}

dependencies {
    implementation(libs.commons.math3)
    implementation(libs.commons.lang3)
    implementation(libs.guava)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.byte.buddy)
//    implementation(libs.vavr)
    implementation(libs.dateparser)
    implementation(libs.lombok)
    implementation(libs.jakarta.validation.api)

    testImplementation(libs.hamcrest)
    testImplementation(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    annotationProcessor(libs.lombok)
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.11.3")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}