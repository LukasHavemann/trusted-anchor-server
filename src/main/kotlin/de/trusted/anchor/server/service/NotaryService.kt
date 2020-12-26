package de.trusted.anchor.server.service

import de.trusted.anchor.server.base.BatchedWorkerPool
import de.trusted.anchor.server.repository.SignedHash
import de.trusted.anchor.server.repository.SignedHashRepository
import de.trusted.anchor.server.service.proof.CollectionService
import de.trusted.anchor.server.service.timestamping.TimestampingService
import org.bouncycastle.tsp.TimeStampResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.util.function.Tuple2
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.transaction.Transactional

@Service
class NotaryService : Loggable {

    @Autowired
    private lateinit var timestampingService: TimestampingService

    @Autowired
    private lateinit var collectionService: CollectionService

    @Autowired
    private lateinit var repository: SignedHashRepository

    private val batchedWorkerPool: BatchedWorkerPool<SigningRequest, TimeStampResponse> = BatchedWorkerPool()

    @PostConstruct
    fun init() {
        collectionService.setSerialNumber(repository.getMaxId() ?: 0)
        batchedWorkerPool.start(::handleBatch)
    }

    @PreDestroy
    fun destroy() {
        batchedWorkerPool.shutdown()
    }

    @Transactional
    fun handleBatch(workToDo: List<Tuple2<SigningRequest, MonoProcessor<TimeStampResponse>>>) {
        val toBeInserted = ArrayList<SignedHash>(workToDo.size)
        val toBeNotfied = ArrayList<Runnable>(workToDo.size)

        logger().debug("processing batch of " + workToDo.size)
        collectionService.publish(workToDo.stream().map { it.t1 }.collect(Collectors.toList()))
        for (work in workToDo) {
            val signingRequest = work.t1
            val response = timestampingService.timestamp(signingRequest)
            toBeInserted.add(
                SignedHash(
                    signingRequest.id!!,
                    signingRequest.receivedAt.toInstant(),
                    signingRequest.hash,
                    "some",
                    response.encoded
                )
            )
            toBeNotfied.add(Runnable {
                work.t2.onNext(response)
            })
        }

        repository.saveAll(toBeInserted)

        toBeNotfied.forEach({ it.run() })
    }

    fun sign(signingRequest: SigningRequest): Mono<TimeStampResponse> {
        logger().trace("signing request accepted " + signingRequest)
        return batchedWorkerPool.add(signingRequest)
    }
}