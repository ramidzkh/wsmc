plugins {
    id("fabric-loom") version "0.2.6-SNAPSHOT"
}

group = "me.ramidzkh"
version = "0.0.1"

minecraft {
    refmapName = "mixins.wsmc.refmap.json"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("net.minecraft", "minecraft", "1.15.1")
    mappings("net.fabricmc", "yarn", "1.15.1+build.24", classifier = "v2")
    modCompile("net.fabricmc", "fabric-loader", "0.7.3+build.176")
}

tasks.jar {
    from("LICENSE")
}
