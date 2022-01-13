package net.yakclient.client.boot.exception

public class RestrictedClassException(name: String) : Exception("Class $name is restricted to loading in this module!")