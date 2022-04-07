plugins {
    kotlin("jvm") version "1.6.10"
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
        kotlinOptions.jvmTarget = "17"
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.compileJava {
        targetCompatibility = "17"
        sourceCompatibility = "17"
    }

    dependencies {
        implementation(kotlin("stdlib"))
        testImplementation(kotlin("test"))
    }

    kotlin {
        explicitApi()
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs = listOf("--add-reads","kotlin.stdlib=kotlinx.coroutines.core.jvm")
    }
}