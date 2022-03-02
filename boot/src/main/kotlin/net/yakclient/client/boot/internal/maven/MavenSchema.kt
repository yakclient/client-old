package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.schema.*
import net.yakclient.client.util.isReachable
import net.yakclient.client.util.resource.SafeResource
import java.net.URI
import java.net.URL

private typealias C = MavenSchemeContext

internal abstract class MavenSchema : Schema<C> {
    override val handler: SchemeHandler<C> = SchemeHandler()

    open val meta by abstractScheme<C, SafeResource>()
    open val versionedArtifact by abstractScheme<C, URL?>()
    open val artifact by abstractScheme<C, URL>()
    open val pom by abstractScheme<C, SafeResource?>()
    open val jar by abstractScheme<C, SafeResource?>()

    override fun validate(c: C): ContextHandler<C>? = ContextHandler(c).run {
        if (!get(artifact).isReachable()) null else this//throw InvalidSchemaException("Artifact: '${c.project.group}:${c.project.artifact}:${c.project.version}' not found")
    }
}

internal data class MavenSchemeContext(
    val project: MavenDescriptor
) : Schema.Context

