import net.yakclient.client.boot.internal.InternalLayoutProvider;
import net.yakclient.client.boot.internal.InternalRepoProvider;
import net.yakclient.client.boot.maven.layout.MavenLayoutProvider;
import net.yakclient.client.boot.repository.RepositoryProvider;

module yakclient.client.boot {
    requires kotlin.stdlib;
    requires kotlinx.cli.jvm;
    requires java.logging;
    requires config4k;
    requires typesafe.config;
    requires transitive yakclient.client.util;
    requires kotlin.reflect;
    requires java.xml;
    requires kotlinx.coroutines.core.jvm;
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;
    requires yakclient.archives;

    // TODO remove, just for insuring that the modules are in the module graph
    requires jdk.unsupported;
    requires java.instrument;
    requires jdk.attach;
    requires java.sql;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.extension;
    exports net.yakclient.client.boot.exception;
    exports net.yakclient.client.boot.dependency;
    exports net.yakclient.client.boot.repository;
    exports net.yakclient.client.boot.loader;
    exports net.yakclient.client.boot.maven.layout;
    exports net.yakclient.client.boot.container;
    exports net.yakclient.client.boot.container.security;
    exports net.yakclient.client.boot.internal.fs to java.base;
    exports net.yakclient.client.boot.container.volume;

    exports net.yakclient.client.boot.internal to java.base; // Service loading
    exports net.yakclient.client.boot.maven;

    opens net.yakclient.client.boot.repository to kotlin.reflect; // For kotlin CLI
    opens net.yakclient.client.boot.internal to java.base; // For service instantiation
    opens net.yakclient.client.boot.maven.pom to com.fasterxml.jackson.databind, kotlin.reflect; // For xml and Json parsing \/
    opens net.yakclient.client.boot.dependency to com.fasterxml.jackson.databind;
    opens net.yakclient.client.boot.container.volume to com.fasterxml.jackson.databind;

    uses RepositoryProvider;
    uses MavenLayoutProvider;

    provides RepositoryProvider with InternalRepoProvider;
    provides MavenLayoutProvider with InternalLayoutProvider;
}