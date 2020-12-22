package de.trusted.anchor.server.service.timestamping

import de.trusted.anchor.server.service.Loggable
import de.trusted.anchor.server.service.logger
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampResponse
import org.bouncycastle.tsp.TimeStampToken
import org.bouncycastle.util.encoders.Hex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.time.Instant
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

    fun signHash(request: TimestampRequest): TimestampResponse {
        val request = timestampingContext.get().timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, request.hash)
        val now = Date()
        val response = timestampingContext.get().timeStampResponseGenerator.generate(
            request,
            BigInteger.valueOf(serialNumber.incrementAndGet()),
            now
        )
        return TimestampResponse(now.toInstant(), response)
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

class TimestampRequest {

    var hash: ByteArray

    constructor(hex: String) {
        hash = Hex.decode(hex)
    }

    constructor(hash: ByteArray) {
        this.hash = hash
    }
}

data class TimestampResponse(
    val time: Instant,
    val token: TimeStampResponse
)