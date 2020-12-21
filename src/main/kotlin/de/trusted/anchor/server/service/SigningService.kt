package de.trusted.anchor.server.service

import de.trusted.anchor.server.SHA256DigestCalculator
import de.trusted.anchor.server.repository.SignedHashRepository
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.tsp.*
import org.bouncycastle.util.Store
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

/**
 * @author Lukas Havemann
 */
@Service
class SigningService {

    @Autowired
    lateinit var repository: SignedHashRepository

    @Autowired
    lateinit var keyService: KeyService

    lateinit var tsRespGen: TimeStampResponseGenerator

    lateinit var certificate: X509Certificate

    val reqGen = TimeStampRequestGenerator()

    val serialNumber: AtomicLong = AtomicLong()

    @PostConstruct
    fun init() {
        val privateKey: PrivateKey = keyService.readPrivateKey("test", "src/main/resources/myCA.key")
        certificate = keyService.readCertificate("src/main/resources/rootCA.pem")
        val certs: Store<*> = JcaCertStore(Arrays.asList(certificate))

        val tsTokenGen = TimeStampTokenGenerator(
                JcaSimpleSignerInfoGeneratorBuilder().build("SHA256withRSA", privateKey, certificate),
                SHA256DigestCalculator(),
                ASN1ObjectIdentifier("1.2"))

        tsTokenGen.addCertificates(certs)
        tsRespGen = TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED)
    }

    fun signHash(hash: String): TimeStampResponse {
        val request = reqGen.generate(TSPAlgorithms.SHA256, ByteArray(32))
        return tsRespGen.generate(request, BigInteger.valueOf(serialNumber.incrementAndGet()), Date())
    }

    fun validate(tsToken: TimeStampToken): Boolean {
        try {
            tsToken.validate(JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(certificate))
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}