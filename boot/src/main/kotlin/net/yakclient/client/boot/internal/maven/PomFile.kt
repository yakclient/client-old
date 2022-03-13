package net.yakclient.client.boot.internal.maven

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class MavenDependency(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val scope: String?,
) {
    fun toDescriptor(): MavenDescriptor = MavenDescriptor(groupId, artifactId, version)
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PomParent(
    val groupId: String,
    val artifactId: String,
    val version: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonDeserialize(using = RepositoryDeserializer::class)
internal data class Pom(
    val groupId: String?,
    val artifactId: String,
    val version: String?,
    val properties: Map<String, String>?,
    val parent: PomParent?,
    val dependencies: Set<MavenDependency>?,
    val repositories: Set<PomRepository>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class PomRepository(
    val url: String
)
//
//private class RepositoryDeserializer : JsonDeserializer<Pom>() {
//    private inline fun <reified T> ObjectCodec.treeToValue(node: TreeNode) = treeToValue(node, T::class.java)
////    private inline fun <reified T> ObjectCodec.readValue(node: TreeNode) = readValue(node, T::class.java)
//
//    fun getString(name: String, tree: TreeNode) = (tree.get(name) as? ValueNode)?.asText()
//
//    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Pom {
//        val tree = p.codec.readTree<TreeNode>(p)
//
//        fun getString(name: String) = getString(name, tree)
//
//        return Pom(
//            getString("groupId"),
//            getString("artifactId")!!,
//            getString("version"),
//            tree["properties"]?.let { p.codec.treeToValue(it) } ?: HashMap(),
//            tree["parent"]?.let { p.codec.treeToValue<PomParent>(it) },
//            (tree["dependencies"]?.get("dependency") as? ObjectNode)?.mapTo(HashSet()) { p.codec.treeToValue<MavenDependency>(it) } ?: HashSet(),
//            (tree["repositories"]?.get("repository") as? ArrayNode)?.mapTo(HashSet()) { getString("url") ?: throw IllegalArgumentException("Invalid repository in pom. (no more info)") } ?: HashSet()
//        )
//    }

//}