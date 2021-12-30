package net.yakclient.client.api.ext

//TODO figure out jvm default for this
public interface Extension {
    public fun onLoad() {  }

    public fun onEnable() {  }

    public fun onDisable() {  }
}