module yakclient.client.internal {
    requires kotlin.stdlib;
    requires typesafe.config;

    exports net.yakclient.client.internal.extension;
    exports net.yakclient.client.internal.lifecycle;
    exports net.yakclient.client.internal.setting;
}