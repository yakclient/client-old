module yakclient.client.util {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.xml;
    requires config4k;
    requires typesafe.config;
    requires kotlinx.coroutines.core.jvm;
    requires java.logging;
    requires kotlin.reflect;

    exports net.yakclient.client.util;
    exports net.yakclient.client.util.resource;
}