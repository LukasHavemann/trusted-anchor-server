package de.trusted.anchor.server.repository

import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * @author Lukas Havemann
 */
interface SignedHashRepository : CrudRepository<SingedHash, Long> {
    //  fun findBySignedAt(singedAt: Instant): SingedHash?
}

@Entity
data class SingedHash(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val SignedAt: Instant,
    val hashValue: String,
    val application: String
)