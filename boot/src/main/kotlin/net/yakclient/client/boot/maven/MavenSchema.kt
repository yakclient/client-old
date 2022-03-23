package net.yakclient.client.boot.maven

import net.yakclient.client.util.Schema
import net.yakclient.client.util.SchemaMeta
import net.yakclient.client.util.resource.SafeResource

//internal typealias MvnC = MavenArtifactContext

public interface MavenSchema : Schema<MavenArtifactContext> {

    public val meta: SchemaMeta<MavenArtifactContext, SafeResource>// by abstractScheme<C, SafeResource>()

    //    open val versionedArtifact by abstractScheme<C, URL?>()
//    open val artifact by abstractScheme<C, URL>()
    public val pom: SchemaMeta<MavenVersionContext, SafeResource>
    public val jar: SchemaMeta<MavenVersionContext, SafeResource?>

//    override fun validate(c: C): ContextHandler<C>? = ContextHandler(c).run {
//        if (!get<Any, Schema.Context>(jar).isReachable()) null else this//throw InvalidSchemaException("Artifact: '${c.project.group}:${c.project.artifact}:${c.project.version}' not found")
//    }
}

public open class MavenArtifactContext(
    public val group: String,
    public val artifact: String,
) : Schema.Context {
    override fun toString(): String = "MavenArtifactContext(group='$group', artifact='$artifact')"
}


public open class MavenVersionContext(
    group: String,
    artifact: String,
    public val version: String
) : MavenArtifactContext(group, artifact) {
    public constructor(ac: MavenArtifactContext, version: String) : this(ac.group, ac.artifact, version)

    override fun toString(): String = "MavenVersionContext(version='$version', group='$artifact', version='$version')"
}
