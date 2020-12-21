package de.trusted.anchor.server

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ess.SigningCertificateV2
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMDecryptorProvider
import org.bouncycastle.openssl.PEMEncryptedKeyPair
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
import org.bouncycastle.operator.DigestCalculator
import org.bouncycastle.tsp.*
import org.bouncycastle.util.Store
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.math.BigInteger
import java.security.PrivateKey
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*


@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    // BlockHound.install();
    // runApplication<ServerApplication>(*args)

    val inputS: InputStream = FileInputStream("src/main/resources/rootCA.pem")
    val factory = CertificateFactory.getInstance("X.509")
    val cert = factory.generateCertificate(inputS) as X509Certificate

    val privateKey: PrivateKey = readPrivateKey(File("src/main/resources/myCA.key")) as PrivateKey
    val certs: Store<*> = JcaCertStore(Arrays.asList(cert))

    val tsTokenGen = TimeStampTokenGenerator(
            JcaSimpleSignerInfoGeneratorBuilder().build("SHA256withRSA", privateKey, cert),
            SHA256DigestCalculator(),
            ASN1ObjectIdentifier("1.2"))

    tsTokenGen.addCertificates(certs)

    val reqGen = TimeStampRequestGenerator()
    val request = reqGen.generate(TSPAlgorithms.SHA256, ByteArray(32), BigInteger.valueOf(100))

    val tsRespGen = TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED)

    var tsResp = tsRespGen.generate(request, BigInteger("23"), Date())

    print(tsResp.status)

    tsResp = TimeStampResponse(tsResp.encoded)

    val tsToken = tsResp.timeStampToken

    tsToken.validate(JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(cert))

    val table = tsToken.signedAttributes

    print(table[PKCSObjectIdentifiers.id_aa_signingCertificateV2])

    val digCalc: DigestCalculator = SHA256DigestCalculator()

    val dOut = digCalc.outputStream

    dOut.write(cert.encoded)

    dOut.close()

    val certHash = digCalc.digest

    val sigCertV2 = SigningCertificateV2.getInstance(table[PKCSObjectIdentifiers.id_aa_signingCertificateV2].attributeValues[0])
}

@Throws(Exception::class)
fun readPrivateKey(file: File?): PrivateKey {
    Security.addProvider(BouncyCastleProvider())

    val pemParser = org.bouncycastle.openssl.PEMParser(FileReader(file))
    val `object`: Any = pemParser.readObject()
    val converter: JcaPEMKeyConverter = JcaPEMKeyConverter().setProvider("BC")
    if (`object` is PEMEncryptedKeyPair) {
        // Encrypted key - we will use provided password
        val ckp: PEMEncryptedKeyPair = `object` as PEMEncryptedKeyPair
        val decProv: PEMDecryptorProvider = JcePEMDecryptorProviderBuilder().build("test".toCharArray())
        return converter.getKeyPair(ckp.decryptKeyPair(decProv)).private
    }
    // Unencrypted key - no password needed
    val ukp: PEMKeyPair = `object` as PEMKeyPair
    return converter.getKeyPair(ukp).private
}