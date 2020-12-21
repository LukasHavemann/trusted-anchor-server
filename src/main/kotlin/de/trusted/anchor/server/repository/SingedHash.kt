package de.trusted.anchor.server.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * @author Lukas Havemann
 */
@Repository
interface SignedHashRepository : CrudRepository<SingedHash, Long> {
    //  fun findBySignedAt(singedAt: Instant): SingedHash?

    @Query(value = "SELECT max(id) FROM SingedHash")
    fun getMaxId(): Long?
}

@Entity
data class SingedHash(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val signedAt: Instant,
    val hashValue: String,
    val application: String,
    val timestampToken: ByteArray
)