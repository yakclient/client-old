package net.yakclient.client.boot.dep

public interface DependencyReference {
    public val classloader: ClassLoader
    public val name: String
}