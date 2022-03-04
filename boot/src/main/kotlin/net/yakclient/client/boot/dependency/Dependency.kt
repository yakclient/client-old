package net.yakclient.client.boot.dependency

import net.yakclient.client.util.resource.SafeResource
import java.net.URI

public class Dependency(
        public val jar: SafeResource,
        public val dependants: List<Descriptor>,
        public val desc: Descriptor
) {
    public interface Descriptor {
        public val artifact: String
        public val version: String?
    }
}

