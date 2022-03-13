package net.yakclient.client.boot.internal.maven

import net.yakclient.client.boot.schema.*
import net.yakclient.client.util.resource.SafeResource

//internal typealias MvnC = MavenArtifactContext

internal interface MavenSchema : Schema<MavenArtifactContext> {

    val meta : SchemaMeta<MavenArtifactContext, SafeResource>// by abstractScheme<C, SafeResource>()
//    open val versionedArtifact by abstractScheme<C, URL?>()
//    open val artifact by abstractScheme<C, URL>()
    val pom : SchemaMeta<MavenVersionContext, SafeResource>
    val jar : SchemaMeta<MavenVersionContext, SafeResource>

//    override fun validate(c: C): ContextHandler<C>? = ContextHandler(c).run {
//        if (!get<Any, Schema.Context>(jar).isReachable()) null else this//throw InvalidSchemaException("Artifact: '${c.project.group}:${c.project.artifact}:${c.project.version}' not found")
//    }
}

internal open class MavenArtifactContext(
    val group: String,
    val artifact: String,
) : Schema.Context

internal open class MavenVersionContext(
//    ac: MavenArtifactContext,
    group: String,
    artifact: String,
    val version: String
) : MavenArtifactContext(group, artifact) {
    constructor(ac: MavenArtifactContext, version:String) : this(ac.group,ac.artifact,version)
}

