package de.trusted.anchor.server.service

import de.trusted.anchor.server.repository.SignedHashRepository
import de.trusted.anchor.server.repository.SingedHash
import org.bouncycastle.tsp.TimeStampResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class NotaryService {

    @Autowired
    lateinit var timestampingService: TimestampingService

    @Autowired
    lateinit var repository: SignedHashRepository

    @PostConstruct
    fun init() {
        timestampingService.setSerialNumber(repository.getMaxId() ?: 0)
    }


    fun sign(hash: String): TimeStampResponse {
        val signHash = timestampingService.signHash(hash)
        repository.save(
            SingedHash(
                0,
                signHash.timeStampToken.timeStampInfo.genTime.toInstant(),
                hash,
                "some",
                signHash.timeStampToken.encoded
            )
        )
        return signHash;
    }
}