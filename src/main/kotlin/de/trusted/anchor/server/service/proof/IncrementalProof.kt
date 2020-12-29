package de.trusted.anchor.server.service.proof

import de.trusted.anchor.server.base.BinaryHelper
import de.trusted.anchor.server.service.timestamping.SHA256DigestCalculator
import org.bouncycastle.operator.DigestCalculator
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.*


/**
 * (N0), (N1, I0,1), (N2), (N3, I2.1, I0.2)
 */
class IncrementalProof(
    private val outputStream: OutputStream,
    private val factory: Function1<Unit, DigestCalculator> = { SHA256DigestCalculator() }
) {

    private var hashCounter: Int = 0
    private var maxFulltreeWidth = 0
    private val hashes: Stack<ByteArray> = Stack()

    fun add(hash: ByteArray) {
        writeAndRemember(hash)
        if (++hashCounter % 2 == 0) {
            writeAndRemember(hashTwoTopElements())

            // reached new tree width
            if (BinaryHelper.isPowerOfTwo(hashCounter)) {
                writeRecursive(hashCounter / 2)
                maxFulltreeWidth = hashCounter
                return
            }

            // generate hashes for completed subtree
            val subtreeWidth = BinaryHelper.findBiggestBinaryTree(hashCounter - maxFulltreeWidth, maxFulltreeWidth)
            writeRecursive(subtreeWidth / 2)
        }
    }

    fun finish(): ByteArray {
        if (this.isEmpty()) {
            throw IllegalStateException("proof is empty")
        }

        if (hashCounter == 1) {
            val digestCalculator = factory.invoke(Unit)
            digestCalculator.outputStream.write(hashes.pop())
            writeAndRemember(digestCalculator.digest)
            return hashes.pop()
        }

        while (hashes.size != 1) {
            writeAndRemember(hashTwoTopElements())
        }

        // write width of tree
        outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(hashCounter).array())

        return hashes.pop()
    }

    private fun writeRecursive(value: Int) {
        if (value <= 1) {
            return
        }

        writeAndRemember(hashTwoTopElements())
        writeRecursive(value / 2)
    }

    private fun writeAndRemember(hash: ByteArray) {
        outputStream.write(hash)
        hashes.add(hash)
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
        return hashCounter == 0
    }
}
