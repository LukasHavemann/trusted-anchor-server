package de.trusted.anchor.server.service

import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*


class SigningRequest(
    appName: String,
    val eventId: Int,
    hashStr: String
) {
    val receivedAt = Date()
    val hash: ByteArray = validate(hashStr)
    val appName: String = appName.padStart(8, ' ').substring(0, 8)

    @Volatile
    var id: Long? = null
        private set

    private fun validate(hash: String): ByteArray {
        if (hash.length != 64) {
            throw IllegalArgumentException(String.format("invalid hash '%s' %d", hash, hash.length))
        }

        try {
            return Hex.decode(hash)
        } catch (ex: Exception) {
            throw IllegalArgumentException(hash, ex)
        }
    }

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