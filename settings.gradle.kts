rootProject.name = "ExNet"

val kotlin_version: String by settings

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "org.jetbrains.kotlin" -> useVersion(kotlin_version)
            }
        }
    }
}