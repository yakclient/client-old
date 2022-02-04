package net.yakclient.client.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import io.github.config4k.ClassContainer
import io.github.config4k.CustomType
import java.io.File
import java.net.URI

public interface ReadOnlyType : CustomType {
    override fun testToConfig(obj: Any): Nothing = throw UnsupportedOperationException()

    override fun toConfig(obj: Any, name: String): Nothing = throw UnsupportedOperationException()
}

public abstract class TypedMatchingType<T>(
    private val type: Class<T>
) : CustomType {
    override fun testParse(clazz: ClassContainer): Boolean = type.isAssignableFrom(clazz.mapperClass.java)
}

public class UriCustomType : CustomType {
    override fun parse(clazz: ClassContainer, config: Config, name: String): Any =
        URI(config.getString(name))

    override fun testParse(clazz: ClassContainer): Boolean =
        URI::class.java.isAssignableFrom(clazz.mapperClass.java)

    override fun testToConfig(obj: Any): Boolean = obj is URI

    override fun toConfig(obj: Any, name: String): Config =
        ConfigFactory.parseMap(mapOf(name to obj.toString()))// throw UnsupportedOperationException("Not supported")
}

public fun URI.toConfig(): Config = ConfigFactory.parseURL(toURL())

public fun Config.writeTo(
    file: File,
    options: ConfigRenderOptions = ConfigRenderOptions.concise().setJson(false).setFormatted(true)
): Unit = file.writeText(this.root().render(options))