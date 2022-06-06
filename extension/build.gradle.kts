group = "net.yakclient"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":api"))
    implementation(project(":internal"))
    api("net.yakclient:archives:1.0-SNAPSHOT") {
        isChanging = true
    }
}

tasks.test {
    systemProperty("java.nio.file.spi.DefaultFileSystemProvider", "net.yakclient.client.extension.test.TestFileSystemOverloading\$MyFileSystemProvider")
    jvmArgs = listOf(
        "--add-opens",
        "yakclient.client.extension/net.yakclient.client.extension.test=java.base",
    )
}