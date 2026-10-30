pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SpamGuardLite"

include(":app")
include(":core:domain")
include(":core:data")
include(":core:testing")
include(":feature:calllog")
include(":feature:blocklist")
