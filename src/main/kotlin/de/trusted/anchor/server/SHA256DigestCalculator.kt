package de.trusted.anchor.server

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.operator.DigestCalculator
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class SHA256DigestCalculator : DigestCalculator {
    private val bOut = ByteArrayOutputStream()

    override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
        return AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)
    }

    override fun getOutputStream(): OutputStream {
        return bOut
    }

    override fun getDigest(): ByteArray {
        val bytes = bOut.toByteArray()
        bOut.reset()
        val sha256: Digest = SHA256Digest()
        sha256.update(bytes, 0, bytes.size)
        val digest = ByteArray(sha256.digestSize)
        sha256.doFinal(digest, 0)
        return digest
    }
}