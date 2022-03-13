group = "net.yakclient"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":util"))

    implementation(project(":api"))
    implementation(project(":boot"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6")

}