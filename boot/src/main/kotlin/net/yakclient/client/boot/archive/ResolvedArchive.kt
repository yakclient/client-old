package net.yakclient.client.boot.archive

public interface ResolvedArchive {
    public val classloader: ClassLoader
    public val name: String
}