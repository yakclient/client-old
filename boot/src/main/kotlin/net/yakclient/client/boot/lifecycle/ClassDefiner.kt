package net.yakclient.client.boot.lifecycle

// TODO possibly take out
public interface ClassDefiner {
    public fun defineClass(name: String, bytes: ByteArray) : Class<*>
}