package net.yakclient.client.util.resource

import net.yakclient.client.util.openStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.security.MessageDigest
import java.util.logging.Level
import java.util.logging.Logger

private const val NUM_ATTEMPTS = 3

public class ExternalResource(
    override val uri: URI,
    private val check: ByteArray,
    checkType: String = "SHA1"
) : SafeResource {
    private val resource: ByteArray

    init {
        val logger = Logger.getLogger(ExternalResource::class.simpleName)
        assert(NUM_ATTEMPTS > 0)

        fun <T> doUntil(attempts: Int, supplier: () -> T?): T? {
            for (i in 0 until attempts) {
                supplier()?.let { return@doUntil it }
            }
            return null
        }

        val digest = MessageDigest.getInstance(checkType)

        resource = doUntil(NUM_ATTEMPTS) {
            digest.reset()

            uri.openStream().use { uriIn ->
                logger.log(Level.FINE, "Loading resource: $uri into memory for checksum processing")
                val b = uriIn.readAllBytes()

                digest.update(b)

                if (digest.digest().contentEquals(check)) {
                    b
                } else {
                    logger.log(Level.WARNING, "Checksums failed for resource: $uri")
                    null
                }
            }
        } ?: throw DownloadFailedException(uri)
    }

    override fun open(): InputStream = ByteArrayInputStream(resource)
}

