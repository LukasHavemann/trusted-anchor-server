package de.trusted.anchor.server.service.publication

import de.trusted.anchor.server.repository.PublicationRepository
import de.trusted.anchor.server.repository.SignedHash
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class PublicationService {

    @Autowired
    private lateinit var publicationRepository: PublicationRepository

    private val toBePublished: Queue<List<SignedHash>> = ConcurrentLinkedQueue()

    fun register(toBeInserted: List<SignedHash>) {
        toBePublished.offer(toBeInserted)
    }

    fun publish() {

        while (true) {
            val element = toBePublished.poll()
            if (element != null) {

            }
        }
    }
}