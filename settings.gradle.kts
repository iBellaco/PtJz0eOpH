pluginManagement {
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.google.devtools.ksp") {
        useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
      }
    }
  }

  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Coach"

include(":app")
