package de.trusted.anchor.server.controller

import de.trusted.anchor.server.service.NotaryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author Lukas Havemann
 */
@RestController
class ImportController {

    @Autowired
    lateinit var notaryService: NotaryService

    @GetMapping(
        path = ["/running"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @ResponseBody
    fun getRunning() = "trusted anchor is running"

    @GetMapping(
        path = ["/signHash/{hash}"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun signHash(@PathVariable("hash") hash: String): Mono<ByteArray> {
        return notaryService.sign(hash).map { it.timeStampToken.encoded }
    }
}