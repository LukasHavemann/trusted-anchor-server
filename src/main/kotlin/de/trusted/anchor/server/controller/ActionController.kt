package de.trusted.anchor.server.controller

import de.trusted.anchor.server.service.NotaryService
import de.trusted.anchor.server.service.SigningRequest
import de.trusted.anchor.server.service.proof.CollectionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * @author Lukas Havemann
 */
@RestController
class ActionController {

    @Autowired
    private lateinit var notaryService: NotaryService

    @Autowired
    private lateinit var collectionService: CollectionService

    @GetMapping(
        path = ["/running"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @ResponseBody
    fun getRunning() = "trusted anchor is running"


    @GetMapping(
        path = ["/new/round"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @ResponseBody
    fun createNewRound() {
        collectionService.newRound()
    }


    @GetMapping(
        path = ["/sign/hash/{hash}"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun signHash(
        @PathVariable("hash") hash: String,
        @RequestParam appName: String,
        @RequestParam eventId: Int
    ): Mono<ByteArray> {
        return notaryService
            .sign(SigningRequest(appName, eventId, hash))
            .map { it.timeStampToken.encoded }
    }
}