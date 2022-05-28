group = "net.yakclient"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation(kotlin("reflect"))
    api("net.yakclient:common-util:1.0-SNAPSHOT") {
        isChanging = true
    }
//    implementation("org.apache.httpcomponents:httpclient:4.5.13")

}
