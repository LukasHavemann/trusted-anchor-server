package de.trusted.anchor.server.service.proof

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

internal class IncrementalProofReaderTest {

    @Test
    fun readFromProof() {
        val softly = SoftAssertions()
        IncrementalProofReader("src/test/resources/inc-test.testproof").use { reader ->
            softly.assertThat(reader.readHash().eventId).isEqualTo(56)
            softly.assertThat(reader.readHash().eventId).isEqualTo(57)
            softly.assertThat(reader.readProof()).isNotNull
            softly.assertThat(reader.readHash().eventId).isEqualTo(58)
            softly.assertThat(reader.readHash().eventId).isEqualTo(59)

            reader.reset()
            softly.assertThat(reader.readProofHead()).isNotNull
            reader.reset()
        }
        softly.assertAll()
    }

    @Test
    fun readAll() {
        IncrementalProofReader("src/test/resources/inc-test.testproof").use { reader ->
            while (reader.hasNext()) {
                println(reader.readNextHash())
            }

            reader.readLastProofs()
            assertThat(reader.readHashes.size).isEqualTo(11)
        }
    }
}