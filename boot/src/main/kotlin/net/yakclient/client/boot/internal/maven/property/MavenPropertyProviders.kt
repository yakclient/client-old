package net.yakclient.client.boot.internal.maven.property

import com.fasterxml.jackson.databind.ObjectMapper
import net.yakclient.client.boot.internal.maven.MavenDescriptor
import net.yakclient.client.boot.internal.maven.MavenSchema
import net.yakclient.client.boot.internal.maven.MavenSchemeContext
import net.yakclient.client.boot.internal.maven.Pom
import org.xml.sax.SAXException
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.readValue


private fun Pom.tryLoadParent(mapper: ObjectMapper, schema: MavenSchema): Pom? {
    val parent = this.parent?.let {
        MavenDescriptor(
            it.groupId,
            it.artifactId,
            it.version // Kill me now if any of these are property substitutions... I can't deal with it...
        )
    } ?: return null

    return try {
        //schema.validate(MavenSchemeContext(parent))?.get(schema.POM) ?: return null
//        builder.parse(loadArtifact(repo, parent).pom.openStream()).documentElement

        mapper.readValue<Pom>(
            (schema.validate(MavenSchemeContext(parent))?.get(schema.pom) ?: return null).open()
        ).also {
            it.groupId = it.groupId ?: it.tryLoadParent(mapper, schema)?.groupId ?: throw IllegalStateException("Failed to load groupId of artifact: ${it.artifactId}")
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } catch (e: SAXException) {
        e.printStackTrace()
        null
    }
}

private fun <T> Pom.travelParents(mapper: ObjectMapper, repo: MavenSchema, call: (Pom) -> T?): T? =
    call(this) ?: (this.tryLoadParent(mapper, repo))?.travelParents(mapper, repo, call)

internal class PomPropertyProvider(
    private val mapper: ObjectMapper,
    private val schema: MavenSchema
) : MavenPropertyProvider {
    override fun provide(pom: Pom, property: String): String? =
        pom.travelParents(mapper, schema) { it.properties?.get(property) }
}

//internal class LocalPomProvider : MavenPropertyProvider {
//    override fun provide(document: Element, property: String): String? =
//        document["properties"].firstOrNull()?.valueOf(property)
//}
//
//internal class ParentPomProvider(
//    private val builder: DocumentBuilder,
//    private val repo: String
//) : MavenPropertyProvider {
//    override fun provide(document: Element, property: String): String? =
//        document.tryLoadParent(builder, repo)?.get("properties")?.firstOrNull()?.valueOf(property)
//}

internal abstract class ConstantPropertyProvider(
    private val provides: Set<String>
) : MavenPropertyProvider {
    override fun provide(pom: Pom, property: String): String? =
        if (provides.contains(property)) provide(pom) else null

    abstract fun provide(document: Pom): String?
}


internal class PomVersionProvider(
    private val mapper: ObjectMapper,
    private val schema: MavenSchema
) : MavenPropertyProvider {
    override fun provide(pom: Pom, property: String): String? = when (property) {
        "project.version" -> pom.travelParents(mapper, schema) { it.version }
        "project.parent.version" -> pom.tryLoadParent(mapper, schema)?.version
        else -> null
    }
}

//internal class ParentVersionProvider(
//    private val builder: DocumentBuilder,
//    private val repo: String
//) : ConstantPropertyProvider("project.parent.version") {
//    override fun provide(document: Element): String? = document.tryLoadParent(builder, repo)?.valueOf("version")
//}
//
//internal class ProjectVersionProvider(
//    private val builder: DocumentBuilder,
//    private val repo: String
//) : ConstantPropertyProvider(setOf("project.version")) {
//    override fun provide(document: Element): String? =
//        document.valueOf("version") ?: document.tryLoadParent(builder, repo)?.valueOf("version")
//}