module yakclient.client.api {
    requires kotlin.stdlib;
    requires yakclient.client.boot;
    requires config4k;
    requires typesafe.config;
    requires yakclient.client.util;

    exports net.yakclient.client.api.ext.specific to yakclient.client.boot;
}