package de.trusted.anchor.server.service

import de.trusted.anchor.server.SHA256DigestCalculator
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponseGenerator
import org.bouncycastle.tsp.TimeStampTokenGenerator
import org.bouncycastle.util.Store
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*
import javax.annotation.PostConstruct


@Service
class SigningContextFactory {

    @Autowired
    lateinit var keyService: KeyService

    lateinit var privateKey: PrivateKey

    lateinit var certificate: X509Certificate

    lateinit var certs: Store<*>

    @PostConstruct
    fun init() {
        privateKey = keyService.readPrivateKey("test", "src/main/resources/myCA.key")
        certificate = keyService.readCertificate("src/main/resources/rootCA.pem")
        certs = JcaCertStore(Arrays.asList(certificate))
    }

    fun getContext(): SigningContext {
        val tsTokenGen = TimeStampTokenGenerator(
            JcaSimpleSignerInfoGeneratorBuilder().build("SHA256withRSA", privateKey, certificate),
            SHA256DigestCalculator(),
            ASN1ObjectIdentifier("1.2")
        )

        tsTokenGen.addCertificates(certs)
        return SigningContext(
            TimeStampRequestGenerator(),
            TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED),
            certificate
        )
    }
}

data class SigningContext(
    val timeStampRequestGenerator: TimeStampRequestGenerator,
    val timeStampResponseGenerator: TimeStampResponseGenerator,
    val certificate: X509Certificate
)