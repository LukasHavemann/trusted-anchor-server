package de.trusted.anchor.server.service

import org.bouncycastle.util.encoders.Hex

class HashParser {
    companion object {
        fun parseHexString(hash: String): ByteArray {
            if (hash.length != 64) {
                throw IllegalArgumentException(String.format("invalid hash '%s' %d", hash, hash.length))
            }

            try {
                return Hex.decode(hash)
            } catch (ex: Exception) {
                throw IllegalArgumentException(hash, ex)
            }
        }
    }
}