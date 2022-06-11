package net.yakclient.client.extension

import net.yakclient.client.boot.YakClient
import net.yakclient.client.boot.extension.Extension
import net.yakclient.common.util.make

public class MinecraftExtensionManager internal constructor(
    parent: Extension
) {
    public val trusted: ContainerGroup
    public val thirdParty: ContainerGroup

    init {
        YakClient.settings.trustedExtPath.toFile().mkdirs()
        YakClient.settings.thirdPartyExtPath.toFile().mkdirs()

        trusted = PathWatchingContainerGroup("trusted", YakClient.settings.trustedExtPath, parent)
        thirdParty = PathWatchingContainerGroup("third-party", YakClient.settings.thirdPartyExtPath, parent)
    }
}