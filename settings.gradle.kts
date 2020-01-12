rootProject.name = "wsmc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()

        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}
