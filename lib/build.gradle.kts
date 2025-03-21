plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)
    api(libs.commons.lang3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.byte.buddy)
    implementation(libs.vavr)
    implementation(libs.dateparser)
    implementation(libs.lombok)

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
