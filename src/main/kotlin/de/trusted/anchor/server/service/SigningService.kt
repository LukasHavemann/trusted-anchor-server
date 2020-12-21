package de.trusted.anchor.server.service

import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampResponse
import org.bouncycastle.tsp.TimeStampToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

/**
 * @author Lukas Havemann
 */
@Service
class SigningService {

    @Autowired
    lateinit var signingContextFactory: SigningContextFactory

    lateinit var signingContext: ThreadLocal<SigningContext>

    val serialNumber: AtomicLong = AtomicLong()

    @PostConstruct
    fun init() {
        signingContext = ThreadLocal.withInitial({ -> signingContextFactory.getContext() })
    }

    fun signHash(hash: String): TimeStampResponse {
        val request = signingContext.get().timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, ByteArray(32))
        return signingContext.get().timeStampResponseGenerator.generate(
            request,
            BigInteger.valueOf(serialNumber.incrementAndGet()),
            Date()
        )
    }

    fun validate(tsToken: TimeStampToken): Boolean {
        try {
            tsToken.validate(
                JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(signingContext.get().certificate)
            )

            return true
        } catch (ex: Exception) {
            return false
        }
    }
}