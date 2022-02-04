module yakclient.client.util {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.xml;
    requires config4k;
    requires typesafe.config;
    requires kotlinx.coroutines.core.jvm;

    exports net.yakclient.client.util;

}