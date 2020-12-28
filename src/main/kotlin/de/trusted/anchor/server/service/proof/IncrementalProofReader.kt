package de.trusted.anchor.server.service.proof

import de.trusted.anchor.server.base.BinaryHelper
import org.bouncycastle.util.encoders.Hex
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.util.*

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


class IncrementalProofReader(filepath: String) : AutoCloseable {
    private val raf = RandomAccessFile(filepath, "r")
    private val appNameBuffer = ByteArray(8)
    private val hashBuffer = ByteArray(32)

    private var readPointer: Int = 0
    private var maxFulltreeWidth = readHashCount()

    private val readHashes: List<HashValue> = LinkedList()

    fun readNextHash(): Hash {
        val hash = readHash()
        readHashes.plus(hash)
        if (++readPointer % 2 == 0) {
            // reached new tree width
            if (BinaryHelper.isPowerOfTwo(readPointer)) {
                readProofsRecursive(readPointer)
            } else {
                // generate hashes for completed subtree
                val subtreeWidth = BinaryHelper.findBiggestBinaryTree(readPointer - maxFulltreeWidth, maxFulltreeWidth)
                readProofsRecursive(subtreeWidth / 2)
            }
        }

        return hash
    }

    fun hasNext() : Boolean {
        return readPointer < maxFulltreeWidth
    }

    private fun readProofsRecursive(value: Int) {
        if (value <= 1) {
            return
        }

        readHashes.plus(readProof())
        readProofsRecursive(value / 2)
    }

    fun reset() {
        raf.seek(0)
    }

    fun readProofHead(): Proof {
        // 32 byte hash + 4 byte size
        raf.seek(raf.length() - 36)
        return readProof()
    }

    private fun readHashCount(): Int {
        raf.seek(raf.length() - 4)
        val hashCount = raf.readInt()
        raf.seek(0)
        return hashCount
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

    override fun close() {
        raf.close()
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