import net.yakclient.client.boot.ext.ExtensionLoader;
import net.yakclient.client.boot.internal.ExtJpmFinder;
import net.yakclient.client.boot.internal.ExtJpmResolver;
import net.yakclient.client.boot.internal.InternalRepoProvider;
import net.yakclient.client.boot.internal.maven.provider.MavenPropertyProvider;
import net.yakclient.client.boot.repository.RepositoryProvider;

module yakclient.client.boot {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.logging;
    requires config4k;
    requires typesafe.config;
    requires yakclient.client.util;
    requires kotlin.reflect;

    requires java.xml;
//    requires com.fasterxml.jackson.core;
//    requires com.fasterxml.jackson.databind;
//    requires com.fasterxml.jackson.dataformat.xml;

    requires yakclient.bmu.api;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.ext;
    exports net.yakclient.client.boot.setting;
    exports net.yakclient.client.boot.lifecycle;
    exports net.yakclient.client.boot.exception;
    opens net.yakclient.client.boot.repository to kotlin.reflect;

    uses ExtensionLoader.Finder;
    uses ExtensionLoader.Resolver;
    uses RepositoryProvider;

    provides ExtensionLoader.Finder with ExtJpmFinder;
    provides ExtensionLoader.Resolver with ExtJpmResolver;
    provides RepositoryProvider with InternalRepoProvider;
}