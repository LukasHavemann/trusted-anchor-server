package de.trusted.anchor.server.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.math.BigInteger
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

/**
 * @author Lukas Havemann
 */
@Repository
interface SignedHashRepository : CrudRepository<SignedHash, Long> {

    @Query(value = "SELECT max(id) FROM SignedHash")
    fun getMaxId(): Long?
}

@Entity
data class SignedHash(
    @Id
    val id: Long,
    val signedAt: Instant,
    val hashValue: ByteArray,
    val application: String,
    val timestampToken: ByteArray
)