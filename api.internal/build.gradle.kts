group = "net.yakclient"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    implementation(project(":util"))

    implementation(project(":api"))
    implementation(project(":boot"))

    implementation("net.yakclient:archive-mapper:1.0-SNAPSHOT")
    api("net.yakclient:archives:1.0-SNAPSHOT") {
        isChanging = true
    }
    implementation("net.yakclient:archives-mixin:1.0-SNAPSHOT") {
        isChanging = true
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6")
}