package de.trusted.anchor.server.service

import org.bouncycastle.asn1.ASN1ObjectIdentifier

class Constants {
    companion object {
        // private OID https://tools.ietf.org/html/rfc1155#section-3.1.4

        val APP_NAME_OID = ASN1ObjectIdentifier("1.3.6.1.4.1.1")
        val EVENT_ID_OID = ASN1ObjectIdentifier("1.3.6.1.4.1.2")
    }
}