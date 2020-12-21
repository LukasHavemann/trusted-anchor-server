package de.trusted.anchor.server.controller

import de.trusted.anchor.server.service.SigningService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * @author Lukas Havemann
 */
@RestController
class ImportController {

    @Autowired
    lateinit var signingService: SigningService

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
    fun signHash(@PathVariable("hash") hash: String): ByteArray {
        val signedHash = signingService.signHash(hash)
        return signedHash.encoded
    }
}