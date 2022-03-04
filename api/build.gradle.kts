group = "net.yakclient"
version = "1.0-SNAPSHOT"

kotlin {

}
dependencies {
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("com.typesafe:config:1.4.1")
    implementation(project(":boot"))
    implementation(project(":util"))

//    api("net.yakclient:graphics-api:1.0-SNAPSHOT")
//    api("net.yakclient:bmu-mixin:1.0-SNAPSHOT")
//    api("net.yakclient:event-api:1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.test {
    useJUnitPlatform()
}
