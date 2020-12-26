package de.trusted.anchor.server.service.publication

import de.trusted.anchor.server.repository.Publication
import de.trusted.anchor.server.repository.PublicationRepository
import de.trusted.anchor.server.service.proof.CollectionRound
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PublicationService {

    @Autowired
    private lateinit var publicationRepository : PublicationRepository

    fun publish(collectionRound : CollectionRound) {
        publicationRepository.save(
            Publication(
                0,
                collectionRound.roundStart,
                collectionRound.roundFinish!!,
                collectionRound.head!!,
                null,
                null
            )
        )
    }
}