plugins {
    application
}

group = "net.yakclient"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    api("net.yakclient:archives:1.0-SNAPSHOT") {
        isChanging = true
    }
    implementation(kotlin("reflect"))

    implementation("io.github.config4k:config4k:0.4.2")
    implementation(project(":util"))

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6")
}

application {
    mainClass.set("net.yakclient.client.boot.YakClientKt")
    mainModule.set("yakclient.client.boot")

    applicationDefaultJvmArgs = listOf(
        "--add-reads",
        "kotlin.stdlib=kotlinx.coroutines.core.jvm",
        "--add-exports",
        "kotlin.reflect/kotlin.reflect.jvm.internal=com.fasterxml.jackson.kotlin",
        "-Xms512m",
        "-Xmx2G",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseG1GC",
        "-XX:G1NewSizePercent=20",
        "-XX:G1ReservePercent=20",
        "-XX:MaxGCPauseMillis=50",
        "-XX:G1HeapRegionSize=32M",
        "-XstartOnFirstThread"
    )
}

tasks.run.get().workingDir = project.parent!!.rootDir.toPath().resolve("workingDir").toFile()
