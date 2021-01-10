package de.trusted.anchor.server.service.publication

internal class EtherumPublishProviderTest {

    // @Test
    fun callTransaction() {
        EtherumPublishProvider().sendTransaction("hash", "cid")
    }
}