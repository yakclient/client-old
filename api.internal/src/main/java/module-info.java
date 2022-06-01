module yakclient.client.api.internal {
    requires kotlin.stdlib;
    requires yakclient.client.api;
    requires yakclient.client.boot;
    requires yakclient.client.util;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.kotlin;
    requires kotlinx.coroutines.core.jvm;
    requires java.logging;
    requires transitive yakclient.archives;
    requires yakclient.archives.mixin;
    requires yakclient.archive.mapper;
//    requires yakclient.graphics.api;
//    requires yakclient.graphics.components;
//    requires yakclient.graphics.lwjgl;
//    requires yakclient.graphics.lwjgl.util;
//    requires yakclient.graphics.lwjgl.components;

    opens net.yakclient.client.api.internal to com.fasterxml.jackson.databind;
    exports net.yakclient.client.api.internal;
}