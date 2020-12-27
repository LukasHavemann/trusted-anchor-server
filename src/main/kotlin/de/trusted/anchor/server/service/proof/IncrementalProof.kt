package de.trusted.anchor.server.service.proof

import de.trusted.anchor.server.service.timestamping.SHA256DigestCalculator
import org.bouncycastle.operator.DigestCalculator
import java.io.OutputStream
import java.util.*


/**
 * (N0), (N1, I0,1), (N2), (N3, I2.1, I0.2)
 */
class IncrementalProof(
    val outputStream: OutputStream,
    val factory: Function1<Unit, DigestCalculator> = { SHA256DigestCalculator() }
) {

    private var id: Int = 0
    private var maxFulltreeWidth = 0
    private val hashes: Stack<ByteArray> = Stack()

    fun add(hash: ByteArray) {
        writeAndRememberNode(hash)
        if (++id % 2 == 0) {
            writeAndRememberProof(hashTwoTopElements())

            // reached new tree width
            if (isPowerOfTwo(id)) {
                writeRecursive(id / 2)
                maxFulltreeWidth = id
                return
            }

            // generate hashes for completed subtree
            val subtreeWidth = findBiggestBinaryTree(id - maxFulltreeWidth, maxFulltreeWidth)
            writeRecursive(subtreeWidth / 2)
        }
    }

    fun finish(): ByteArray {
        if (this.isEmpty()) {
            throw IllegalStateException("proof is empty")
        }

        if (hashes.size == 1) {
            return hashes.pop()
        }

        if (hashes.size >= 2) {
            writeAndRememberProof(hashTwoTopElements())
        }

        return finish()
    }

    private fun findBiggestBinaryTree(subtree: Int, treeWidth: Int): Int {
        if (subtree < 0) {
            return -1
        }

        if (isPowerOfTwo(subtree)) {
            return subtree
        }

        return findBiggestBinaryTree(subtree - (treeWidth / 2), treeWidth / 2)
    }

    private fun writeRecursive(value: Int) {
        if (value <= 1) {
            return
        }

        writeAndRememberProof(hashTwoTopElements())
        writeRecursive(value / 2)
    }

    private fun writeAndRememberProof(hash: ByteArray) {
        outputStream.write(hash)
        hashes.add(hash)
    }

    private fun writeAndRememberNode(hash: ByteArray) {
        outputStream.write(hash)
        hashes.add(hash)
    }

    private fun isPowerOfTwo(x: Int): Boolean {
        return x and (x - 1) == 0
    }

    private fun hashTwoTopElements(): ByteArray {
        val digestCalculator = factory.invoke(Unit)
        val last = hashes.pop()
        val beforeLast = hashes.pop()
        digestCalculator.outputStream.write(beforeLast)
        digestCalculator.outputStream.write(last)
        return digestCalculator.digest
    }

    fun isEmpty(): Boolean {
        return id == 0
    }
}