plugins {
    kotlin("jvm") version "1.6.0"
    id("org.javamodularity.moduleplugin") version "1.8.10"
}

group = "net.yakclient"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

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

    tasks.compileJava {
        targetCompatibility = "17"
        sourceCompatibility = "17"
    }

    dependencies {
        implementation(kotlin("stdlib"))
    }

    kotlin {
        explicitApi()
    }
}