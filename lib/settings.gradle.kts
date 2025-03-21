rootProject.name = "options"
group = "io.github.fiserro"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}