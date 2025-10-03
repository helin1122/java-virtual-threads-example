import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.spring.io/release")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.spring.io/release")
    }
}

rootProject.name = "java-virtual-threads-example"
