package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.common.util.resource.SafeResource

public class Dependency(
    public val jar: SafeResource?,
    public val dependants: Set<Transitive>,
    public val desc: Descriptor
) {
    public data class Transitive(
        public val possibleRepos: List<RepositorySettings>,
        public val desc: Descriptor,
    )

    public interface Descriptor {
        public val artifact: String
        public val version: String?

        public fun toPrettyString(): String = "$artifact:$version"
    }
}

