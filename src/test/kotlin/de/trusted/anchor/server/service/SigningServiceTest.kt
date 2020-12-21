package de.trusted.anchor.server.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.Instant

@SpringBootTest
internal class SigningServiceTest {

    @Autowired
    lateinit var signingService: SigningService

    @Test
    fun simpleSmokeTest() {
        val timeStampResponse = signingService.signHash("aSimpleHash")
        assertTrue(signingService.validate(timeStampResponse.timeStampToken))
    }

    @Test
     fun isThreadSafe() {
        runBlocking {
            (0..1000).map {
                async(Dispatchers.Default) {
                    val timeStampResponse = signingService.signHash("aSimpleHash" + it)
                    assertTrue(signingService.validate(timeStampResponse.timeStampToken))
                }
            }.awaitAll()
        }
    }

    @Test
    fun simpleSmokRunBatchedTest() {
        val before = Instant.now()
        runBlocking { // limits the scope of concurrency
            (0..100).map { // is a shorter way to write IntRange(0, 10)
                async(Dispatchers.Default) { // async means "concurrently", context goes here
                    for (i in (0..100)) {
                        val timeStampResponse = signingService.signHash("aSimpleHash" + i * it + it)
                        assertTrue(signingService.validate(timeStampResponse.timeStampToken))
                    }
                }
            }.awaitAll() // waits all of them
        }

        val after = Instant.now()
        print(Duration.between(before, after))
    }

    @Test
    fun simpleSingleThreadedSmokRuneTest() {
        val before = Instant.now()
        for (j in 0..10000) {
            val timeStampResponse = signingService.signHash("aSimpleHash" + j)
            assertTrue(signingService.validate(timeStampResponse.timeStampToken))
        }

        val after = Instant.now()
        print(Duration.between(before, after))
    }
}
