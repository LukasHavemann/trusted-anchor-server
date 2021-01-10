package de.trusted.anchor.server.service.publication

interface PublishProvider {

    fun publish(hash: ByteArray, cid: String)
}