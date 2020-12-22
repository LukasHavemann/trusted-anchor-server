package de.trusted.anchor.server.service

import de.trusted.anchor.server.base.Batcher
import de.trusted.anchor.server.repository.SignedHash
import de.trusted.anchor.server.repository.SignedHashRepository
import de.trusted.anchor.server.service.timestamping.TimestampingService
import org.bouncycastle.tsp.TimeStampResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.util.function.Tuple2
import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Service
class NotaryService : Loggable {

    @Autowired
    lateinit var timestampingService: TimestampingService

    @Autowired
    lateinit var repository: SignedHashRepository

    private val batcher: Batcher<String, TimeStampResponse> = Batcher()

    @PostConstruct
    fun init() {
        timestampingService.setSerialNumber(repository.getMaxId() ?: 0)
        batcher.start(::handleBatch)
    }

    @Transactional
    fun handleBatch(workToDo: List<Tuple2<String, MonoProcessor<TimeStampResponse>>>) {
        val toBeInserted = ArrayList<SignedHash>(workToDo.size)
        val toBeNotfied = ArrayList<Runnable>(workToDo.size)

        logger().fine({ "processing batch of " + workToDo.size })
        for (work in workToDo) {
            val hash = work.t1
            val signHash = timestampingService.signHash(hash)
            toBeInserted.add(
                SignedHash(
                    0,
                    signHash.timeStampToken.timeStampInfo.genTime.toInstant(),
                    hash,
                    "some",
                    signHash.timeStampToken.encoded
                )
            )
            toBeNotfied.add(Runnable {
                work.t2.onNext(signHash)
            })
        }

        repository.saveAll(toBeInserted)

        toBeNotfied.forEach({ it.run() })
    }

    fun sign(hash: String): Mono<TimeStampResponse> {
        logger().finer({ "signing request accepted " + hash })
        return batcher.add(hash)
    }
}