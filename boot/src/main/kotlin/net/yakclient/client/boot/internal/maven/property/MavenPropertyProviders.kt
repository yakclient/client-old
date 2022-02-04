package net.yakclient.client.boot.internal.maven.property

import net.yakclient.client.boot.internal.maven.MavenArtifact.Companion.loadArtifact
import net.yakclient.client.boot.internal.maven.MavenProject
import net.yakclient.client.util.get
import net.yakclient.client.util.openStream
import net.yakclient.client.util.valueOf
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

private fun Element.tryLoadParent(builder: DocumentBuilder, repo: String): Element? {
    val parent = this["parent"].firstOrNull()?.let {
        MavenProject(
            it.valueOf("groupId")!!,
            it.valueOf("artifactId")!!,
            it.valueOf("version")!!, // Kill me now if any of these are property substitutions... I can't deal with it...
        )
    } ?: return null

    return try {
        builder.parse(loadArtifact(repo, parent).pom.openStream()).documentElement
    } catch (e: IOException) {
        null
    } catch (e: SAXException) {
        e.printStackTrace()
        null
    }
}

private fun <T> Element.travelParents(builder: DocumentBuilder, repo: String, call: (Element) -> T?): T? =
    call(this) ?: (this.tryLoadParent(builder, repo))?.travelParents(builder, repo, call)

internal class PomPropertyProvider(
    private val builder: DocumentBuilderFactory,
    private val repo: String
): MavenPropertyProvider {
    override fun provide(document: Element, property: String): String? = document.travelParents(builder.newDocumentBuilder(), repo) { it["properties"].firstOrNull()?.valueOf(property) }
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
    override fun provide(document: Element, property: String): String? =
        if (provides.contains(property)) provide(document) else null

    abstract fun provide(document: Element): String?
}


internal class PomVersionProvider(
    private val builder: DocumentBuilderFactory,
    private val repo: String
) : MavenPropertyProvider {
    override fun provide(document: Element, property: String): String? = when(property) {
        "project.version" -> document.travelParents(builder.newDocumentBuilder(), repo) { it.valueOf("version") }
        "project.parent.version" -> document.tryLoadParent(builder.newDocumentBuilder(), repo)?.valueOf("version")
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