package de.trusted.anchor.server.service.proof

import org.bouncycastle.util.encoders.Hex
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

/**
 *  1,  2 = 0
 *  3,  4 = 1
 *  5,  6 = 3
 *  7,  8 = 4
 *  9, 10 = 7
 * 11, 12 = 8
 * 13, 14 = 10
 * 15, 16 = 11
 * 17, 18 = 15
 * 19, 20 = 16
 * 21, 22 = 18
 * 23, 24 = 19
 * 25, 26 = 22
 * 27, 28 = 23
 */

/**
 * inkl. Knoten
 *  1,  2 = 0
 *  3,  4 = 3
 *  5,  6 = 7
 *  7,  8 = 10
 *  9, 10 = 15
 * 11, 12 = 18
 * 13, 14 = 22
 * 15, 16 = 11
 * 17, 18 = 15
 * 19, 20 = 16
 * 21, 22 = 18
 * 23, 24 = 19
 * 25, 26 = 22
 * 27, 28 = 23
 */

/**
 *  1,  2 = 0
 *  5,  6 = 3
 *  9, 10 = 7
 * 13, 14 = 10
 * 17, 18 = 15
 * 21, 22 = 18
 * 25, 26 = 22
 */


class IncrementalProofReader(filepath: String) {
    private val raf = RandomAccessFile(filepath, "r")
    private val appNameBuffer = ByteArray(8)
    private val hashBuffer = ByteArray(32)

    fun reset() {
        raf.seek(0)
    }

    fun readProofHead(): Proof {
        raf.seek(raf.length() - 32)
        return readProof()
    }

    fun readHash(): Hash {
        // sum of bytes: 8 + 8 + 4 + 32 = 52
        val id = raf.readLong()
        raf.read(appNameBuffer)
        val eventId = raf.readInt()
        raf.read(hashBuffer)
        return Hash(id, String(appNameBuffer, StandardCharsets.US_ASCII), eventId, hashBuffer.clone())
    }

    fun readProof(): Proof {
        raf.read(hashBuffer)
        return Proof(hashBuffer.clone())
    }
}

data class Hash(
    val id: Long,
    val appName: String,
    val eventId: Int,
    override val hash: ByteArray
) : HashValue {
    override fun toString(): String {
        return "Hash(id=$id, appName='$appName', eventId=$eventId, hash=${
            String(Hex.encode(hash), StandardCharsets.UTF_8)
        })"
    }
}

data class Proof(
    override val hash: ByteArray
) : HashValue {
    override fun toString(): String {
        return "Proof(hash=${
            String(Hex.encode(hash), StandardCharsets.UTF_8)
        })"
    }
}

interface HashValue {
    val hash: ByteArray
}