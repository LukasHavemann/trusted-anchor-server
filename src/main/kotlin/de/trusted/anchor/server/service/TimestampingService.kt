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
class TimestampingService : Loggable {

    @Autowired
    lateinit var signingContextFactory: SigningContextFactory

    lateinit var timestampingContext: ThreadLocal<TimestampingContext>

    val serialNumber: AtomicLong = AtomicLong()

    fun setSerialNumber(serialNumber: Long) {
        logger().info("setting serialnumber to " + serialNumber)
        this.serialNumber.set(serialNumber)
    }

    @PostConstruct
    fun init() {
        timestampingContext = ThreadLocal.withInitial({ -> signingContextFactory.getContext() })
    }

    fun signHash(hash: String): TimeStampResponse {
        val request = timestampingContext.get().timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, ByteArray(32))
        return timestampingContext.get().timeStampResponseGenerator.generate(
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
                    .build(timestampingContext.get().certificate)
            )

            return true
        } catch (ex: Exception) {
            return false
        }
    }
}