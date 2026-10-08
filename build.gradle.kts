plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "net.zithium"
version = "3.8.6"
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
    implementation("com.github.cryptomorin:XSeries:13.8.0")
    implementation("javax.inject:javax.inject:1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.github.BGMP.CommandFramework:command-framework-bukkit:master") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    implementation("com.tcoded:FoliaLib:0.5.2")
    implementation("de.tr7zw:item-nbt-api:2.16.1") // UPDATE THIS FOR EACH NEW MC VERSION
    implementation("org.bstats:bstats-bukkit-lite:1.8")
    implementation("com.github.shynixn.headdatabase:hdb-api:1.0")
    implementation("com.github.ItzSave:ZithiumLibrary:2.1.1")

    compileOnly("io.papermc.paper:paper-api:26.3-rc-3.build.1-alpha")
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.4")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("me.clip:placeholderapi:2.12.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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
        relocate("net.kyori.adventure", "net.zithium.deluxehub.libs.adventure")
        relocate("com.cryptomorin.xseries", "net.zithium.deluxehub.libs.xseries")
    }
}
