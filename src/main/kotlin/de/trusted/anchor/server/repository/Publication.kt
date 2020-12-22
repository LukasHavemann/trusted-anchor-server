package de.trusted.anchor.server.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
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
    val fromId: Long,
    /**
     * from id of SignedHash
     */
    val toId : Long,
    val hashId : Long,
    val publicationId : String,
    val publicationSystem : String
)