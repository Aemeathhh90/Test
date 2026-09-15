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

sourceControl {
    gitRepository("https://github.com/Aemeathhh90/vider.git") {
        producesModule("com.kakaanime:provider")
    }
}

rootProject.name = "KakaAnime"
include(":app")
