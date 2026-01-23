plugins {
    id("java")
    id("idea")
    id("maven-publish")
}

group = "com.example"
version = "0.1.0"

val javaVersion = 25
val appData = System.getenv("APPDATA") ?: ""
val hytaleAssets = file("$appData/Hytale/install/pre-release/package/game/latest/Assets.zip")
val hytaleServerJar = file("serverCode/HytaleServer.jar")

repositories {
    mavenCentral()
    flatDir {
        dirs("serverCode")
    }
}

dependencies {
    if (hytaleServerJar.exists()) {
        compileOnly(files(hytaleServerJar))
    } else {
        logger.warn("Hytale Server Jar not found at: ${hytaleServerJar.absolutePath}")
    }

    if (hytaleAssets.exists()) {
        compileOnly(files(hytaleAssets))
    } else {
        logger.warn("Hytale Assets.zip not found at: ${hytaleAssets.absolutePath}")
    }
}

tasks.register<JavaExec>("runServer") {
    group = "hytale"
    description = "Runs HytaleServer.jar with Assets.zip."

    workingDir = file("run")
    standardInput = System.`in`

    doFirst {
        require(hytaleServerJar.exists()) { "Missing `${hytaleServerJar.absolutePath}`" }
        require(hytaleAssets.exists()) { "Missing Assets at `${hytaleAssets.absolutePath}`" }
    }

    mainClass.set("-jar")
    args(
        hytaleServerJar.absolutePath,
        "--assets", hytaleAssets.absolutePath,
        "--disable-sentry",
        "--log", "ALL"
    )
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
