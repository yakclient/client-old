plugins {
    kotlin("jvm") version "1.6.0"
    id("org.javamodularity.moduleplugin") version "1.8.10"

}

group = "net.yakclient"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.wrapper {
    gradleVersion = "7.3.1"
}

//dependencies {
//    implementation(kotlin("stdlib"))
//}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin="org.javamodularity.moduleplugin")


    repositories {
//        maven {
//            url = uri("https://repo1.maven.org/maven2")
//
//        }
        mavenCentral()
    }
    tasks.compileKotlin {
        destinationDirectory.set(tasks.compileJava.get().destinationDirectory.asFile.get())
        kotlinOptions.jvmTarget = "11"
    }
    dependencies {
        implementation(kotlin("stdlib"))

    }

    kotlin {
        explicitApi()
    }
}