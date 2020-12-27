package de.trusted.anchor.server.service.proof

import de.trusted.anchor.server.service.Loggable
import de.trusted.anchor.server.service.SigningRequest
import de.trusted.anchor.server.service.logger
import de.trusted.anchor.server.service.publication.PublicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.scheduler.Schedulers
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct

@Service
class CollectionService : Loggable {

    private lateinit var collectionRound: CollectionRound

    @Autowired
    private lateinit var publicationService: PublicationService

    private val toBePublished: Queue<List<SigningRequest>> = ConcurrentLinkedQueue()
    private val serialNumber: AtomicLong = AtomicLong()
    private val newRound = AtomicBoolean()
    private val backlog = PriorityQueue<SigningRequest>(Comparator.comparing({ sreq: SigningRequest -> sreq.id!! }))

    private var startPointer: Long = 0
    private val publisher = Schedulers.newSingle("publisher")

    @PostConstruct
    fun init() {
        publisher.schedule(::startCollecting)
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

    fun newRound(): Boolean {
        logger().debug("trigger new round")
        if (!collectionRound.incrementalProof.isEmpty()) {
            newRound.set(true)
        }

        return newRound.get()
    }

    fun startCollecting() {
        logger().info("start collecting")
        collectionRound = CollectionRound()
        while (true) {
            if (newRound.get() && !collectionRound.incrementalProof.isEmpty()) {
                finishOldRoundAndCreateNewOne()
            }

            fillBacklog()
            publishBacklog()

            collectionRound.flush()

            if (toBePublished.isEmpty()) {
                Thread.sleep(10)
            }
        }
    }

    private fun finishOldRoundAndCreateNewOne() {
        this.collectionRound.finish()
        publicationService.publish(this.collectionRound)
        this.collectionRound = CollectionRound()
    }

    private fun publishBacklog() {
        while (true) {
            val itemToPublish = backlog.poll()
            if (itemToPublish == null) {
                break
            }

            if (itemToPublish.id != startPointer) {
                backlog.add(itemToPublish)
                logger().debug("out of order for " + itemToPublish.id)
                break
            }

            collectionRound.add(itemToPublish)
            startPointer++
        }
    }

    private fun fillBacklog() {
        while (backlog.size < 1000) {
            val element = toBePublished.poll()
            if (element == null) {
                break
            }
            backlog.addAll(element)
        }

        if (!backlog.isEmpty()) {
            logger().debug("backlog filled with " + backlog.size)
        }
    }
}

class CollectionRound {
    val roundStart = Instant.now()
    var roundFinish: Instant? = null
        private set

    private val filename = String.format(
        "inc-%s.proof", DateTimeFormatter
            .ofPattern("uuuuMMddHHmmss")
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("UTC"))
            .format(roundStart)
    )

    private val outputStream = BufferedOutputStream(FileOutputStream(filename))
    var incrementalProof = IncrementalProof(outputStream)
        private set
    var head: ByteArray? = null
        private set

    fun add(request: SigningRequest) {
        incrementalProof.add(request.hashForProof())
    }

    fun flush() {
        outputStream.flush()
    }

    fun finish() {
        this.roundFinish = Instant.now()
        head = incrementalProof.finish()
        flush()

        try {
            outputStream.close()
        } catch (e: Exception) {
            logger().error("error during close of " + filename, e)
        }
    }
}