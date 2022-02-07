package net.yakclient.client.boot.exception

public class InvalidSchemaException(message: String? = null) : Exception("Given schema is not valid! ${message?.let { "Because: $it" } ?: ""}")