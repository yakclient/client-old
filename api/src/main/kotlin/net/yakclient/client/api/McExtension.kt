package net.yakclient.client.api

import net.yakclient.client.boot.ext.Extension

public abstract class McExtension : Extension() {
    abstract override fun onLoad()

    public abstract fun onUnload()

    public abstract fun onEnable()

    public abstract fun onDisable()
}