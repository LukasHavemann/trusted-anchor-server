package de.trusted.anchor.server.controller

import de.trusted.anchor.server.service.proof.CollectionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * @author Lukas Havemann
 */
@RestController
class ActionController {

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
}