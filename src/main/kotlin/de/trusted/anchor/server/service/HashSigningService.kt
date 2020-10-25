package de.trusted.anchor.server.service

import de.trusted.anchor.server.repository.SignedHashRepository
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

/**
 * @author Lukas Havemann
 */
class HashSigningService {

    @Autowired
    lateinit var repository: SignedHashRepository

    fun signHash(hash: String): Instant {
        val signingTimestamp = Instant.now()
        return signingTimestamp;
    }
}