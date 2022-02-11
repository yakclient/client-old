package net.yakclient.client.util

import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import java.io.File
import java.net.URI
import java.nio.file.Path

public object FileArgument : ArgType<File>(true) {
    override val description: kotlin.String = "{ java.io.File }"

    override fun convert(value: kotlin.String, name: kotlin.String): File =
        File(value).takeIf { it.exists() }
            ?: throw ParsingException("Option $name expected to be an existing file path. $value is provided.")
}

public object PathArgument : ArgType<Path>(true) {
    override val description: kotlin.String = "{ java.nio.file.Path }"

    override fun convert(value: kotlin.String, name: kotlin.String): Path = Path.of(value)

}

public object URIArgument : ArgType<URI>(true) {
    override val description: kotlin.String = "{ java.net.URI }"

    override fun convert(value: kotlin.String, name: kotlin.String): URI {
       return URI.create(value)
    }
}
