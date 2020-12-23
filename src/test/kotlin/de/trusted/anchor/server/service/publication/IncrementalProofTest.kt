package de.trusted.anchor.server.service.publication

import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.operator.DigestCalculator
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

internal class IncrementalProofTest {

    @Test
    fun simpleTest() {
        val outputStream = ByteArrayOutputStream()
        val testee = IncrementalProof(outputStream) {
            object : DigestCalculator {
                val out = ByteArrayOutputStream()

                init {
                    out.write('['.toInt())
                }

                override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
                    TODO("Not yet implemented")
                }

                override fun getOutputStream(): OutputStream {
                    return out
                }

                override fun getDigest(): ByteArray {
                    out.write(']'.toInt())
                    return out.toByteArray()
                }
            }
        }

        for (i in 1..32) {
            try {
                testee.add(("node" + i).toByteArray(StandardCharsets.UTF_8))
            } catch (ex: Exception) {
                print("ERROR: " + i)
                ex.printStackTrace()
            }
        }

        print(String(outputStream.toByteArray(), StandardCharsets.UTF_8))
    }
}