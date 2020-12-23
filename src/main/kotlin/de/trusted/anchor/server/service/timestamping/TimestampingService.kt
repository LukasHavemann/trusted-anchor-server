package de.trusted.anchor.server.service.timestamping

import de.trusted.anchor.server.service.Loggable
import de.trusted.anchor.server.service.SigningRequest
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampResponse
import org.bouncycastle.tsp.TimeStampToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigInteger
import javax.annotation.PostConstruct

/**
 * @author Lukas Havemann
 */
@Service
class TimestampingService : Loggable {

    @Autowired
    private lateinit var signingContextFactory: SigningContextFactory
    private lateinit var timestampingContext: ThreadLocal<TimestampingContext>

    @PostConstruct
    fun init() {
        timestampingContext = ThreadLocal.withInitial({ -> signingContextFactory.getContext() })
    }

    fun timestamp(request: SigningRequest): TimeStampResponse {
        val tsRequest = timestampingContext.get().timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, request.hash)
        val id = BigInteger.valueOf(request.id!!)
        val response = timestampingContext.get().timeStampResponseGenerator.generate(tsRequest, id, request.receivedAt)
        return response
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
