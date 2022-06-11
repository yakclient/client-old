group = "net.yakclient"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":api"))
    implementation(project(":internal"))
    implementation(project(":boot"))
    implementation(project(":util"))
    api("net.yakclient:archives:1.0-SNAPSHOT") {
        isChanging = true
    }
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

}

tasks.test {
    systemProperty(
        "java.nio.file.spi.DefaultFileSystemProvider",
        "net.yakclient.client.extension.test.TestFileSystemOverloading\$MyFileSystemProvider"
    )
    jvmArgs = listOf(
        "--add-opens",
        "yakclient.client.extension/net.yakclient.client.extension.test=java.base",
    )
}