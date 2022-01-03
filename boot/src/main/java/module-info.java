module yakclient.client.boot {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.logging;

    exports net.yakclient.client.boot.ext;
    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.setting;
    exports net.yakclient.client.boot.lifecycle;
}