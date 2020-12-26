package de.trusted.anchor.server.service

import de.trusted.anchor.server.service.timestamping.TimestampingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class TimestampingServiceTest {

    @Autowired
    lateinit var timestampingService: TimestampingService

    @Test
    fun simpleSmokeTest() {
        val timeStampResponse =
            timestampingService.timestamp(
                SigningRequest("8effc8acf8ebfa15a11efbb4e1a62b3e7cd64f630f3860362361e9e3f064c84e").assignId(
                    1
                )
            )

        assertTrue(timestampingService.validate(timeStampResponse.timeStampToken))
    }

    @Test
    fun isThreadSafe() {
        runBlocking {
            (0..1000).map {
                async(Dispatchers.Default) {
                    val timeStampResponse = timestampingService.timestamp(
                        SigningRequest(
                            String.format(
                                "8effc8acf8ebfa15a11efbb4e1a62b3e7cd64f630f3860362361e9e3f064%04d",
                                it
                            )
                        ).assignId(1)
                    )
                    assertTrue(timestampingService.validate(timeStampResponse.timeStampToken))
                }
            }.awaitAll()
        }
    }
}
