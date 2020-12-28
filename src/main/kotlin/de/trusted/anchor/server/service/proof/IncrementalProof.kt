package de.trusted.anchor.server.service.proof

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

    private var id: Int = 0
    private var maxFulltreeWidth = 0
    private val hashes: Stack<ByteArray> = Stack()

    fun add(hash: ByteArray) {
        writeAndRemember(hash)
        if (++id % 2 == 0) {
            writeAndRemember(hashTwoTopElements())

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

        if (id == 1) {
            val digestCalculator = factory.invoke(Unit)
            digestCalculator.outputStream.write(hashes.pop())
            writeAndRemember(digestCalculator.digest)
            return hashes.pop()
        }

        while (hashes.size != 1) {
            writeAndRemember(hashTwoTopElements())
        }

        outputStream.write(ByteBuffer.allocate(Integer.BYTES).putInt(id).array())

        return hashes.pop()
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

        writeAndRemember(hashTwoTopElements())
        writeRecursive(value / 2)
    }

    private fun writeAndRemember(hash: ByteArray) {
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

/**
1 = 0
2 = 1
3 = 0
4 = 2

5 = 0
6 = 1
7 = 0
8 = 3

9 = 0
10 = 1
11 = 0
12 = 2

13 = 0
14 = 1
15 = 0
16 = 4

17 = 0
18 = 1
19 = 0
20 = 2

21 = 0
22 = 1
23 = 0
24 = 3

25 = 0
26 = 1
27 = 0
28 = 2

29 = 0
30 = 1
31 = 0
32 = 5

 */
