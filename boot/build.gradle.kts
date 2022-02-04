plugins {
    application
}

group = "net.yakclient"
version = "1.0-SNAPSHOT"


dependencies {
//    implementation("net.yakclient:bmu-api:1.0-SNAPSHOT")
//    implementation("net.yakclient:bmu-mixin:1.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation(kotlin("reflect"))

    implementation("io.github.config4k:config4k:0.4.2")
    implementation("com.typesafe:config:1.4.1")
    implementation(project(":util"))

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
}

application {
    mainClass.set("net.yakclient.client.boot.YakClientKt")
    mainModule.set("yakclient.client.boot")

    applicationDefaultJvmArgs = listOf("--add-reads","kotlin.stdlib=kotlinx.coroutines.core.jvm")
}

tasks.run {
    this.run
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("--add-reads","kotlin.stdlib=kotlinx.coroutines.core.jvm")
}