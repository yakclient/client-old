import net.yakclient.client.boot.internal.InternalRepoProvider;
import net.yakclient.client.boot.internal.jpm.JpmFinder;
import net.yakclient.client.boot.internal.jpm.JpmResolver;
import net.yakclient.client.boot.archive.ArchiveFinder;
import net.yakclient.client.boot.archive.ArchiveResolver;
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

    // TODO remove, just for insuring that the modules are in the module graph
    requires jdk.unsupported;
    requires java.instrument;
    requires jdk.attach;
    requires java.sql;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.ext;
    exports net.yakclient.client.boot.setting;
    exports net.yakclient.client.boot.lifecycle;
    exports net.yakclient.client.boot.exception;
    exports net.yakclient.client.boot.dep;
    exports net.yakclient.client.boot.repository;
    exports net.yakclient.client.boot.archive;
    exports net.yakclient.client.boot.loader;

    opens net.yakclient.client.boot.repository to kotlin.reflect; // For kotlin CLI
    opens net.yakclient.client.boot.internal to java.base; // For service instantiation
    exports net.yakclient.client.boot.internal to java.base; // ^
    exports net.yakclient.client.boot.internal.maven to kotlin.reflect; // For config parsing

    uses ArchiveResolver;
    uses ArchiveFinder;
    uses RepositoryProvider;

    provides ArchiveResolver with JpmResolver;
    provides ArchiveFinder with JpmFinder;
    provides RepositoryProvider with InternalRepoProvider;
}