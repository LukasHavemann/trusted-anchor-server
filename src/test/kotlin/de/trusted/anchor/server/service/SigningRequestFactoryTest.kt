package de.trusted.anchor.server.service

import org.assertj.core.api.SoftAssertions
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequest
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

internal class SigningRequestFactoryTest {

    @Test
    fun createBasedOnRFC3161() {
        val tsRequest = prepareTimeStampRequest()
        val request = SigningRequestFactory().create(tsRequest!!)
        val softly = SoftAssertions()
        softly.assertThat(request.appName).isEqualTo("    test")
        softly.assertThat(request.eventId).isEqualTo(1)
        softly.assertThat(String(Hex.encode(request.hash), StandardCharsets.UTF_8)).startsWith("8effc")
        softly.assertAll()
    }

    private fun prepareTimeStampRequest(): TimeStampRequest? {
        val hashAsBytes = HashParser.parseHexString("8effc8acf8ebfa15a11efbb4e1a62b3e7cd64f630f3860362361e9e3f064c84e")
        val timeStampRequestGenerator = TimeStampRequestGenerator()
        timeStampRequestGenerator.addExtension(
            Constants.APP_NAME_OID,
            false,
            "test".toByteArray(StandardCharsets.UTF_8)
        )
        timeStampRequestGenerator.addExtension(
            Constants.EVENT_ID_OID,
            false,
            ByteBuffer.allocate(4).putInt(1).array()
        )

        val tsRequest = timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, hashAsBytes)
        return tsRequest
    }

}