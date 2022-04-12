module yakclient.client.api.internal {
    requires kotlin.stdlib;
    requires yakclient.client.api;
    requires yakclient.client.boot;
    requires yakclient.client.util;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.kotlin;
    requires kotlinx.coroutines.core.jvm;

    opens net.yakclient.client.api.internal to com.fasterxml.jackson.databind;
    exports net.yakclient.client.api.internal;

//    provides net.yakclient.client.boot.repository.RepositoryProvider with net.yakclient.client.api.internal.MojangRepositoryProvider;
}