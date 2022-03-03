package net.yakclient.client.boot.internal.maven

//internal open class MavenProject(
//    val group: String,
//    val artifact: String,
//    val version: String,
//) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as MavenProject
//
//        if (group != other.group) return false
//        if (artifact != other.artifact) return false
//        if (version != other.version) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = group.hashCode()
//        result = 31 * result + artifact.hashCode()
//        result = 31 * result + version.hashCode()
//        return result
//    }
//
//    override fun toString(): String = "MavenProject(group='$group', artifact='$artifact', version='$version')"
//}
//private const val DEFAULT_SCOPE = "runtime"
//
//internal data class MavenDependency(
//    val group: String,
//    override val artifact: String,
//    override val version: String?,
//    val scope: String? = DEFAULT_SCOPE,
//) : Dependency.Descriptor {
//    fun toDescriptor() : MavenDescriptor = MavenDescriptor(group, artifact, version)
//}


