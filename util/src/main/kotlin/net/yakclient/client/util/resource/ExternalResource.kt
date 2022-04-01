package net.yakclient.client.util.resource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.yakclient.client.util.readInputStream
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClients
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.logging.Level
import java.util.logging.Logger

private const val NUM_ATTEMPTS = 3

public class ExternalResource(
    override val uri: URI,
    private val check: ByteArray,
    private val checkType: String = "SHA1"
) : SafeResource {
    private val logger = Logger.getLogger(ExternalResource::class.simpleName)

//    private val resource: ByteArray
//
//    init {
//        val logger = Logger.getLogger(ExternalResource::class.simpleName)
//        assert(NUM_ATTEMPTS > 0)
//
//        fun <T> doUntil(attempts: Int, supplier: (Int) -> T?): T? {
//            for (i in 0 until attempts) {
//                supplier(i)?.let { return@doUntil it }
//            }
//            return null
//        }
//
//        val digest = MessageDigest.getInstance(checkType)
//        HttpClients.custom().build().use { client ->
//            resource = doUntil(NUM_ATTEMPTS) { attempt ->
//                logger.log(Level.FINE, "Loading resource: $uri into memory for checksum processing")
//
//                digest.reset()
//
//                val req = RequestBuilder.get()
//                    .setUri(uri)
//                    .build()
//
//                val b = DigestInputStream(client.execute(req).entity.content, digest).use(InputStream::readInputStream)
//                if (digest.digest().contentEquals(check)) b
//                else {
//                    val attemptsLeft = NUM_ATTEMPTS - (attempt + 1)
//                    logger.log(
//                        Level.WARNING,
//                        "Checksums failed for resource: '$uri'. Attempting $attemptsLeft more time${if (attemptsLeft == 1) "" else "s"}."
//                    )
//                    null
//                }
//            } ?: throw DownloadFailedException(uri)
//        }
//    }

    private fun openInternal(): InputStream {
        assert(NUM_ATTEMPTS > 0)

        fun <T> doUntil(attempts: Int, supplier: (Int) -> T?): T? {
            for (i in 0 until attempts) {
                supplier(i)?.let { return@doUntil it }
            }
            return null
        }

        val digest = MessageDigest.getInstance(checkType)

        return HttpClients.custom().build().use { client ->
            ByteArrayInputStream(doUntil(NUM_ATTEMPTS) { attempt ->
                logger.log(Level.FINE, "Loading resource: $uri into memory for checksum processing")

                digest.reset()

                val req = RequestBuilder.get()
                    .setUri(uri)
                    .build()

                val b = DigestInputStream(client.execute(req).entity.content, digest).use(InputStream::readInputStream)

                if (digest.digest().contentEquals(check)) b
                else {
                    val attemptsLeft = NUM_ATTEMPTS - (attempt + 1)
                    logger.log(
                        Level.WARNING,
                        "Checksums failed for resource: '$uri'. Attempting $attemptsLeft more time${if (attemptsLeft == 1) "" else "s"}."
                    )
                    null
                }
            } ?: throw DownloadFailedException(uri))
        }
    }

    override fun open(): InputStream = openInternal()
}

