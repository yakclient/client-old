import net.yakclient.client.boot.ext.ExtensionLoader;

module yakclient.client.boot {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.logging;
    requires config4k;
    requires typesafe.config;
    requires yakclient.client.util;
    requires kotlin.reflect;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.ext;
    exports net.yakclient.client.boot.setting;
    exports net.yakclient.client.boot.lifecycle;
    exports net.yakclient.client.boot.exception;
    opens net.yakclient.client.boot.repository to kotlin.reflect;

    uses ExtensionLoader.Referencer;
    uses ExtensionLoader.Resolver;

    provides ExtensionLoader.Referencer with net.yakclient.client.boot.lifecycle.ExtJpmReferencer;
    provides ExtensionLoader.Resolver with net.yakclient.client.boot.lifecycle.ExtJpmResolver;
}