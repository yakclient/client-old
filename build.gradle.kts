plugins {
    kotlin("jvm") version "1.6.0"
    id("org.javamodularity.moduleplugin") version "1.8.10"
}

repositories {
    mavenCentral()
}

group = "net.yakclient"
version = "1.0-SNAPSHOT"


tasks.wrapper {
    gradleVersion = "7.3.1"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin="org.javamodularity.moduleplugin")

    repositories {
        mavenLocal()

        mavenCentral()
    }

    tasks.compileKotlin {
        destinationDirectory.set(tasks.compileJava.get().destinationDirectory.asFile.get())
        kotlinOptions.jvmTarget = "11"
    }

    tasks.compileJava {
        targetCompatibility = "11"
        sourceCompatibility = "11"
    }

    dependencies {
        implementation(kotlin("stdlib"))
        testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
    }

    kotlin {
        explicitApi()
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs = listOf("--add-reads","kotlin.stdlib=kotlinx.coroutines.core.jvm")
    }
}