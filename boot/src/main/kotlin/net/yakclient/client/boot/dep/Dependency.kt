package net.yakclient.client.boot.dep

import java.net.URI

public class Dependency(
    public val uri: URI,
    public val dependants: List<Descriptor>,
    public val desc: Descriptor
) {
    public interface Descriptor {
        public val artifact: String
        public val version: String?
    }
}

