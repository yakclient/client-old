package net.yakclient.client.boot.dependency

public interface DependencyReference {
    public val classloader: ClassLoader
    public val name: String
}