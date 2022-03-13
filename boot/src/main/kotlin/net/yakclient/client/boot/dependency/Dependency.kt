package net.yakclient.client.boot.dependency

import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource

public class Dependency(
    public val jar: SafeResource,
    public val dependants: List<Transitive>,
    public val desc: Descriptor
) {
    public data class Transitive(
        public val possibleRepos: Set<RepositorySettings>,
        public val desc: Descriptor,
    )

    public interface Descriptor {
        public val artifact: String
        public val version: String?
    }
}

