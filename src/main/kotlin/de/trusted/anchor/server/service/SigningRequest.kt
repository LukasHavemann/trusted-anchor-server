@file:JvmName("SigningRequest")

package de.trusted.anchor.server.service

import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequest
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class SigningRequestFactory {

    fun create(timestampRequest: TimeStampRequest): SigningRequest {
        return SigningRequest(
            String(timestampRequest.getExtension(Constants.APP_NAME_OID).extnValue.octets, StandardCharsets.UTF_8),
            readIntFrom(timestampRequest.getExtension(Constants.EVENT_ID_OID).extnValue.octets),
            timestampRequest.messageImprintDigest,
            timestampRequest
        )
    }

    private fun readIntFrom(octets: ByteArray): Int {
        val bytes = ByteBuffer.allocate(4)
        bytes.put(octets)
        bytes.position(0)
        return bytes.int
    }

    fun create(appName: String, eventId: Int, hash: String): SigningRequest {
        val hashAsBytes = HashParser.parseHexString(hash)
        val timeStampRequestGenerator = TimeStampRequestGenerator()
        val tsRequest = timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, hashAsBytes)
        return SigningRequest(appName, eventId, hashAsBytes, tsRequest)
    }
}

class SigningRequest(
    appName: String,
    val eventId: Int,
    val hash: ByteArray,
    val tsRequest: TimeStampRequest
) {
    val receivedAt = Date()
    val appName: String = appName.padStart(8, ' ').substring(0, 8)

    @Volatile
    var id: Long? = null
        private set

    fun hashForProof(): ByteArray {
        val data = ByteArrayOutputStream()
        data.write(appName.toByteArray(StandardCharsets.UTF_8))
        data.write(eventId)
        data.write(hash)
        return data.toByteArray()
    }

    fun assignId(id: Long): SigningRequest {
        if (this.id == null) {
            this.id = id
        }

        return this
    }

    override fun toString(): String {
        return String.format("%s: %s", this.id?.toString() ?: "no id yet", String(this.hash, StandardCharsets.UTF_8))
    }
}
