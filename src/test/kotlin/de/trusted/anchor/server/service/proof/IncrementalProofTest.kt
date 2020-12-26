package de.trusted.anchor.server.service.proof

import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.DigestCalculator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

internal class IncrementalProofTest {

    lateinit var outputStream: ByteArrayOutputStream
    lateinit var testee: IncrementalProof

    val expected32 = """
        node1
        node2
        [node1node2]
        node3
        node4
        [node3node4]
        [[node1node2][node3node4]]
        node5
        node6
        [node5node6]
        node7
        node8
        [node7node8]
        [[node5node6][node7node8]]
        [[[node1node2][node3node4]][[node5node6][node7node8]]]
        node9
        node10
        [node9node10]
        node11
        node12
        [node11node12]
        [[node9node10][node11node12]]
        node13
        node14
        [node13node14]
        node15
        node16
        [node15node16]
        [[node13node14][node15node16]]
        [[[node9node10][node11node12]][[node13node14][node15node16]]]
        [[[[node1node2][node3node4]][[node5node6][node7node8]]][[[node9node10][node11node12]][[node13node14][node15node16]]]]
        node17
        node18
        [node17node18]
        node19
        node20
        [node19node20]
        [[node17node18][node19node20]]
        node21
        node22
        [node21node22]
        node23
        node24
        [node23node24]
        [[node21node22][node23node24]]
        [[[node17node18][node19node20]][[node21node22][node23node24]]]
        node25
        node26
        [node25node26]
        node27
        node28
        [node27node28]
        [[node25node26][node27node28]]
        node29
        node30
        [node29node30]
        node31
        node32
        [node31node32]
        [[node29node30][node31node32]]
        [[[node25node26][node27node28]][[node29node30][node31node32]]]
        [[[[node17node18][node19node20]][[node21node22][node23node24]]][[[node25node26][node27node28]][[node29node30][node31node32]]]]
        [[[[[node1node2][node3node4]][[node5node6][node7node8]]][[[node9node10][node11node12]][[node13node14][node15node16]]]][[[[node17node18][node19node20]][[node21node22][node23node24]]][[[node25node26][node27node28]][[node29node30][node31node32]]]]]""".trimIndent()

    val expected12 = """
        node1
        node2
        [node1node2]
        node3
        node4
        [node3node4]
        [[node1node2][node3node4]]
        node5
        node6
        [node5node6]
        node7
        node8
        [node7node8]
        [[node5node6][node7node8]]
        [[[node1node2][node3node4]][[node5node6][node7node8]]]
        node9
        node10
        [node9node10]
        node11
        node12
        [node11node12]
        [[node9node10][node11node12]]
        [[[[node1node2][node3node4]][[node5node6][node7node8]]][[node9node10][node11node12]]]""".trimIndent()

    inner class TestDigester : DigestCalculator {
        val out = ByteArrayOutputStream()

        override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
            TODO("Not yet implemented")
        }

        override fun getOutputStream(): OutputStream {
            return out
        }

        override fun getDigest(): ByteArray {
            return String.format(
                "[%s]\n", out.toString(StandardCharsets.UTF_8)
                    .replace("\n", "")
            ).toByteArray(StandardCharsets.UTF_8)
        }
    }

    @BeforeEach
    private fun prepare() {
        outputStream = ByteArrayOutputStream()
        testee = IncrementalProof(outputStream, { TestDigester() })
    }

    @Test
    fun simpleTest() {
        for (i in 1..32) {
            testee.add(("node" + i + "\n").toByteArray(StandardCharsets.UTF_8))
        }
        assertEquals(expected32, String(outputStream.toByteArray(), StandardCharsets.UTF_8).trim())
        assertEquals(expected32.split("\n").last().trim(), String(testee.finish(), StandardCharsets.UTF_8).trim())
    }

    @Test
    fun finishNotCompletedTree() {
        for (i in 1..12) {
            testee.add(("node" + i + "\n").toByteArray(StandardCharsets.UTF_8))
        }

        testee.finish()
        assertEquals(expected12, String(outputStream.toByteArray(), StandardCharsets.UTF_8).trim())
    }

    @Test
    fun emptyAndSmallTree() {
        assertTrue(testee.isEmpty())

        testee.add("node".toByteArray(StandardCharsets.UTF_8))
        assertEquals("node", String(testee.finish(), StandardCharsets.UTF_8))
        assertFalse(testee.isEmpty())
    }
}