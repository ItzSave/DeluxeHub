plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "net.zithium"
version = "3.7.3"
description = "DeluxeHub"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.tcoded.com/releases")
    maven("https://jitpack.io")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation("com.github.cryptomorin:XSeries:13.0.0")
    implementation("javax.inject:javax.inject:1")
    implementation("javax.annotation:javax.annotation-api:1.2")
    implementation("com.github.BGMP.CommandFramework:command-framework-bukkit:master")
    implementation("com.tcoded:FoliaLib:0.5.1")
    implementation("de.tr7zw:item-nbt-api:2.15.5") // UPDATE THIS FOR EACH NEW MC VERSION
    implementation("org.bstats:bstats-bukkit-lite:1.7")
    implementation("com.github.shynixn.headdatabase:hdb-api:1.0")
    implementation("com.github.ItzSave:ZithiumLibrary:1f5182b77f")

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.1")
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.properties(mapOf("version" to version))
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        minimize {
            exclude(dependency("com.tcoded:FoliaLib:.*"))
        }

        archiveClassifier.set("") // Removes "-all" suffix

        relocate("org.bstats", "net.zithium.deluxehub.libs.metrics")
        relocate("cl.bgmp", "net.zithium.deluxehub.libs.command")
        relocate("com.tcoded.folialib", "net.zithium.deluxehub.libs.folialib")
        relocate("de.tr7zw.changeme.nbtapi", "net.zithium.deluxehub.libs.nbt")
        relocate("net.zithium.library", "net.zithium.deluxehub.libs.library")
        relocate("com.cryptomorin.xseries", "net.zithium.deluxehub.libs.xseries")
    }
}
