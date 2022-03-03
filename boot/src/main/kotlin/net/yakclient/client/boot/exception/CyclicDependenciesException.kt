package net.yakclient.client.boot.exception

import net.yakclient.client.boot.dependency.Dependency

public class CyclicDependenciesException(baseDependency: Dependency.Descriptor) : Exception("Cyclic dependencies found in dependency $baseDependency")