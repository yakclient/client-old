group = "net.yakclient"
version = "1.0-SNAPSHOT"

kotlin {

}
dependencies {
    implementation(project(":boot"))

}
tasks.compileKotlin {
//    freeCompilerArgs = ["-Xjvm-default=compatibility"]
}
