package de.trusted.anchor.server.repository

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
interface PublicationRepository : CrudRepository<Publication, Long> {

}

@Entity
data class Publication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    /**
     * from id of SignedHash
     */
    val startRound: Instant,
    /**
     * from id of SignedHash
     */
    val finishRound : Instant,

    /**
     * head
     */
    val hash: ByteArray,
    val publicationHash : String?,
    val publicationSystem : String?
)