package de.trusted.anchor.server.service

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMDecryptorProvider
import org.bouncycastle.openssl.PEMEncryptedKeyPair
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.security.PrivateKey
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Service
class KeyService {

    @Throws(Exception::class)
    fun readPrivateKey(password: String, filePath: String): PrivateKey {
        Security.addProvider(BouncyCastleProvider())

        val pemParser = org.bouncycastle.openssl.PEMParser(FileReader(File(filePath)))
        val keyPair: Any = pemParser.readObject()
        val converter: JcaPEMKeyConverter = JcaPEMKeyConverter().setProvider("BC")
        if (keyPair is PEMEncryptedKeyPair) {
            // Encrypted key - we will use provided password
            val ckp: PEMEncryptedKeyPair = keyPair
            val decProv: PEMDecryptorProvider = JcePEMDecryptorProviderBuilder().build(password.toCharArray())
            return converter.getKeyPair(ckp.decryptKeyPair(decProv)).private
        }

        // Unencrypted key - no password needed
        val ukp: PEMKeyPair = keyPair as PEMKeyPair
        return converter.getKeyPair(ukp).private
    }

    fun readCertificate(filePath: String): X509Certificate {
        val inputS: InputStream = FileInputStream(filePath)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(inputS) as X509Certificate
    }
}