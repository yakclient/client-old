package net.yakclient.client.util

import com.typesafe.config.Config
import io.github.config4k.ClassContainer
import io.github.config4k.CustomType
import java.io.File
import java.net.URI

public class UriCustomType : CustomType {
    override fun parse(clazz: ClassContainer, config: Config, name: String): Any =
        File(config.getString(name)).toURI()

    override fun testParse(clazz: ClassContainer): Boolean =
        URI::class.java.isAssignableFrom(clazz.mapperClass.java)

    override fun testToConfig(obj: Any): Boolean = false

    override fun toConfig(obj: Any, name: String): Nothing = throw UnsupportedOperationException("Not supported")
}