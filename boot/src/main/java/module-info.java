import net.yakclient.client.boot.dep.DependencyGraph;
import net.yakclient.client.boot.ext.ExtensionLoader;
import net.yakclient.client.boot.internal.ExtJpmFinder;
import net.yakclient.client.boot.internal.ExtJpmResolver;
import net.yakclient.client.boot.internal.InternalRepoProvider;
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
    requires kotlinx.coroutines.core.jvm;

    requires jdk.unsupported;
    requires java.instrument;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.ext;
    exports net.yakclient.client.boot.setting;
    exports net.yakclient.client.boot.lifecycle;
    exports net.yakclient.client.boot.exception;
    exports net.yakclient.client.boot.dep;
    exports net.yakclient.client.boot.repository;

    opens net.yakclient.client.boot.repository to kotlin.reflect; // For kotlin CLI
    opens net.yakclient.client.boot.internal to java.base; // For service instantiation
    exports net.yakclient.client.boot.internal to java.base; // ^
    exports net.yakclient.client.boot.internal.maven to kotlin.reflect; // For config parsing

    uses ExtensionLoader.Finder;
    uses ExtensionLoader.Resolver;
    uses RepositoryProvider;
    uses DependencyGraph;

    provides ExtensionLoader.Finder with ExtJpmFinder;
    provides ExtensionLoader.Resolver with ExtJpmResolver;
    provides RepositoryProvider with InternalRepoProvider;

    provides net.yakclient.client.boot.dep.DependencyGraph with net.yakclient.client.boot.internal.JpmDependencyGraph;
}