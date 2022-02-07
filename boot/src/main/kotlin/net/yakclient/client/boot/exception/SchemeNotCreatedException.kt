package net.yakclient.client.boot.exception

public class SchemeNotCreatedException(schema: String) : Exception("Scheme of: $schema not created/implemented yet!")