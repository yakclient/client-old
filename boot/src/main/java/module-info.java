import net.yakclient.client.boot.internal.InternalLayoutProvider;
import net.yakclient.client.boot.internal.InternalRepoProvider;
import net.yakclient.client.boot.internal.ZipReferenceFinder;
import net.yakclient.client.boot.internal.ZipReferenceResolver;
import net.yakclient.client.boot.internal.jpm.JpmFinder;
import net.yakclient.client.boot.internal.jpm.JpmResolver;
import net.yakclient.client.boot.archive.ArchiveFinder;
import net.yakclient.client.boot.archive.ArchiveResolver;
import net.yakclient.client.boot.maven.layout.MavenLayoutProvider;
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
    requires com.fasterxml.jackson.kotlin;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;

    // TODO remove, just for insuring that the modules are in the module graph
    requires jdk.unsupported;
    requires java.instrument;
    requires jdk.attach;
    requires java.sql;
//    requires ktor.client.core.jvm;
//    requires ktor.client.cio.jvm;

    exports net.yakclient.client.boot;
    exports net.yakclient.client.boot.extension;
    exports net.yakclient.client.boot.lifecycle;
    exports net.yakclient.client.boot.exception;
    exports net.yakclient.client.boot.dependency;
    exports net.yakclient.client.boot.repository;
    exports net.yakclient.client.boot.archive;
    exports net.yakclient.client.boot.loader;
    exports net.yakclient.client.boot.maven.layout;

    exports net.yakclient.client.boot.internal to java.base; // Service loading
    exports net.yakclient.client.boot.maven;
//            to
//            kotlin.reflect, // For config parsing
//            com.fasterxml.jackson.databind, // For xml and Json parsing
//            yakclient.client.api.internal; // Use of custom repositories

    opens net.yakclient.client.boot.repository to kotlin.reflect; // For kotlin CLI
    opens net.yakclient.client.boot.internal to java.base; // For service instantiation
    opens net.yakclient.client.boot.maven to com.fasterxml.jackson.databind; // For xml and Json parsing
    opens net.yakclient.client.boot.dependency to com.fasterxml.jackson.databind;

    uses ArchiveResolver;
    uses ArchiveFinder;
    uses RepositoryProvider;
    uses MavenLayoutProvider;

    provides ArchiveResolver with JpmResolver, ZipReferenceResolver;
    provides ArchiveFinder with JpmFinder, ZipReferenceFinder;

    provides RepositoryProvider with InternalRepoProvider;
    provides MavenLayoutProvider with InternalLayoutProvider;
}