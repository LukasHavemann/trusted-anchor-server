package de.trusted.anchor.server.service

import org.bouncycastle.util.encoders.Hex
import java.util.*

class SigningRequest {
    val receivedAt = Date()

    @Volatile
    var id: Long? = null
        private set
    val hash: ByteArray

    constructor(hash: String) {
        // hex reprensentation
        if (hash.length != 64) {
            throw IllegalArgumentException("invalid hash " + hash)
        }

        try {
            this.hash = Hex.decode(hash)
        } catch (ex: Exception) {
            throw IllegalArgumentException(hash, ex)
        }
    }

    constructor(hash: ByteArray) {
        this.hash = hash
    }

    fun assignId(id: Long) {
        if (this.id == null) {
            this.id = id
        }
    }
}