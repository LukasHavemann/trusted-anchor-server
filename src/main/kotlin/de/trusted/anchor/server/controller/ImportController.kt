package de.trusted.anchor.server.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * @author Lukas Havemann
 */
@RestController
class ImportController {

    @GetMapping(
        path = ["/running"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    @ResponseBody
    fun getRunning() = "trusted anchor is running"

    @GetMapping(
        path = ["/signHash"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun signHash(hash: String) {
        "test"
    }
}