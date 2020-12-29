package de.trusted.anchor.server.base

import de.trusted.anchor.server.service.logger
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class BatchedWorkerPool<I, O>(val workerThreads: Int, val batchSize: Int, val maxColletionTimeMs: Int) {

    private val toBeProcessed: Queue<Tuple2<I, MonoProcessor<O>>> = ConcurrentLinkedQueue()
    private val shuttingDown: AtomicBoolean = AtomicBoolean()

    fun start(task: Function1<List<Tuple2<I, MonoProcessor<O>>>, Unit>) {
        (0..workerThreads).forEach({ CoreThread(it, task).start() })
    }

    fun shutdown() {
        shuttingDown.set(true)
    }

    fun collectBatch(batchSize: Int, maxCollectingTimeMs: Int): List<Tuple2<I, MonoProcessor<O>>> {
        val startBatch: Long = System.currentTimeMillis()
        val batch: MutableList<Tuple2<I, MonoProcessor<O>>> = ArrayList()

        // early exit if nothing to do
        var workItem = toBeProcessed.poll()
        if (workItem == null) {
            return batch
        }

        batch.add(workItem)

        while (batch.size < batchSize) {
            workItem = toBeProcessed.poll()
            if (workItem != null) {
                batch.add(workItem)
            } else {
                LockSupport.parkNanos(100_000) // 0.1 ms
                if ((System.currentTimeMillis() - startBatch) > maxCollectingTimeMs) {
                    return batch
                }
            }
        }
        return batch
    }

    @Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER")
    fun add(element: I): Mono<O> {
        val workToDo = Tuples.of<I, MonoProcessor<O>>(element, MonoProcessor.create())
        if (!toBeProcessed.offer(workToDo)) {
            logger().warn("couldnt be processed" + element)
        }
        return workToDo.t2
    }

    inner class CoreThread(id: Int, val task: Function1<List<Tuple2<I, MonoProcessor<O>>>, Unit>) : Thread() {
        init {
            this.name = "core-work-" + id
        }

        override fun run() {
            while (true) {
                val value = collectBatch(batchSize, maxColletionTimeMs)
                if (value.isEmpty()) {
                    sleep(5)
                    continue
                }
                task.invoke(value)

                if (shuttingDown.get()) {
                    return
                }
            }
        }
    }
}