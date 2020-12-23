package de.trusted.anchor.server.service.publication

import de.trusted.anchor.server.repository.PublicationRepository
import de.trusted.anchor.server.service.Loggable
import de.trusted.anchor.server.service.SigningRequest
import de.trusted.anchor.server.service.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.scheduler.Schedulers
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

@Service
class PublicationService : Loggable {

    @Autowired
    private lateinit var publicationRepository: PublicationRepository

    private val toBePublished: Queue<List<SigningRequest>> = ConcurrentLinkedQueue()
    private val serialNumber: AtomicLong = AtomicLong()
    private var startPointer: Long = 0
    private val publisher = Schedulers.newSingle("publisher")
    val outputStream = BufferedOutputStream(FileOutputStream("proof.txt"))
    private var incrementalProof = IncrementalProof(outputStream)

    @PostConstruct
    fun init() {
        publisher.schedule(::publish)
    }

    fun setSerialNumber(serialNumber: Long) {
        logger().info("setting serialnumber to " + serialNumber)
        this.serialNumber.set(serialNumber)
        this.startPointer = serialNumber + 1
    }

    fun publish(request: List<SigningRequest>) {
        request.forEach({ it.assignId(serialNumber.incrementAndGet()) })
        toBePublished.offer(request)
    }

    fun publish() {
        logger().info("start publishing")
        val backlog = PriorityQueue<SigningRequest>(Comparator.comparing({ sreq: SigningRequest -> sreq.id!! }))
        while (true) {
            while (backlog.size < 1000) {
                val element = toBePublished.poll()
                if (element == null) {
                    break
                }
                backlog.addAll(element)
                logger().fine({ "backlog filled with " + backlog.size })
            }


            while (true) {
                val itemToPublish = backlog.poll()
                if (itemToPublish == null) {
                    break
                }

                if (itemToPublish.id != startPointer) {
                    backlog.add(itemToPublish)
                    logger().fine({ "out of order for " + itemToPublish.id })
                    break
                }

                incrementalProof.add(itemToPublish.hash)
                startPointer++
            }

            outputStream.flush()

            if (toBePublished.isEmpty()) {
                Thread.sleep(10)
            }
        }
    }
}