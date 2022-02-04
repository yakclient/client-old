package net.yakclient.client.boot.loader

public interface ClComponent {
    public fun find(name: String) : Class<*>?
}