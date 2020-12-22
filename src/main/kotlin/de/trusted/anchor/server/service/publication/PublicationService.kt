package de.trusted.anchor.server.service.publication

import de.trusted.anchor.server.repository.PublicationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PublicationService {

    @Autowired
    lateinit var publicationRepository: PublicationRepository

}