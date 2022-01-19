plugins {
    application
}

group = "net.yakclient"
version = "1.0-SNAPSHOT"


dependencies {
//    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
//    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1")
//    implementation("org.apache.maven:maven-model:3.8.4")
//    implementation("org.apache.maven:maven-repository-metadata:3.8.4")
//    implementation("org.apache.maven:maven-project:3.0-alpha-2")

    implementation("net.yakclient:bmu-api:1.0-SNAPSHOT")
    implementation("net.yakclient:bmu-mixin:1.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")

    implementation(kotlin("reflect"))

    implementation("io.github.config4k:config4k:0.4.2")
    implementation("com.typesafe:config:1.4.1")
    implementation(project(":util"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

application {
    mainClass.set("net.yakclient.client.boot.YakClientKt")
    mainModule.set("yakclient.client.boot")
}




tasks.test {
    useJUnitPlatform()
}