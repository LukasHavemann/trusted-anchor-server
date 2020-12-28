package de.trusted.anchor.server.service.proof

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

internal class IncrementalProofReaderTest {

    @Test
    fun readFromProof() {
        val reader = IncrementalProofReader("src/test/resources/inc-test.testproof")
        val softly = SoftAssertions()
        softly.assertThat(reader.readHash().eventId).isEqualTo(56)
        softly.assertThat(reader.readHash().eventId).isEqualTo(57)
        softly.assertThat(reader.readProof()).isNotNull
        softly.assertThat(reader.readHash().eventId).isEqualTo(58)
        softly.assertThat(reader.readHash().eventId).isEqualTo(59)

        reader.reset()
        softly.assertThat(reader.readProofHead()).isNotNull
        reader.reset()

        softly.assertThat(reader.readHashAt(5).eventId).isEqualTo(60)
        softly.assertAll()
    }

    @Test
    fun calculateProofCount() {
        val reader = IncrementalProofReader("src/test/resources/inc-test.testproof")
        println(reader.readHashAt(0))
        reader.reset()
        println(reader.readHashAt(1))
        reader.reset()
        println(reader.readHashAt(2))
        reader.reset()
        println(reader.readHashAt(3))
        reader.reset()
        println(reader.readHashAt(4))
        reader.reset()
    }

    @Test
    fun beforeproof() {
        val reader = IncrementalProofReader("src/test/resources/inc-test.testproof")
        (0..33 step 2).forEach({
            println(
                "node " + (it + 1) + ", " + (it + 2) + " proofs " + reader.calculateNumberOfProofsBefore(
                    it
                )
            )
        })

    }
}