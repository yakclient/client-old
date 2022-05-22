module yakclient.client.util {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.xml;
    requires config4k;
    requires typesafe.config;
    requires kotlinx.coroutines.core.jvm;
    requires java.logging;
    requires kotlin.reflect;
    requires transitive yakclient.common.util;

    exports net.yakclient.client.util;
}