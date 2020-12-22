package de.trusted.anchor.server.base

import de.trusted.anchor.server.service.logger
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.LockSupport


internal class Batcher<I, O> {

    val toBeBatched: Queue<Tuple2<I, MonoProcessor<O>>> = ConcurrentLinkedQueue()

    val workerThreads = 16

    fun start(task: Function1<List<Tuple2<I, MonoProcessor<O>>>, Unit>) {
        (0..workerThreads).forEach(
            {
                val thread = Thread() {
                    while (true) {
                        val value = collectBatch()
                        if (value.isEmpty()) {
                            Thread.sleep(10)
                            continue
                        }
                        task.invoke(value)
                    }
                }

                thread.name = "worker" + it
                thread.start()
            })
    }

    fun collectBatch(): List<Tuple2<I, MonoProcessor<O>>> {
        val startBatch: Long = System.currentTimeMillis()
        val batch: MutableList<Tuple2<I, MonoProcessor<O>>> = ArrayList()
        while (batch.size < 5) {
            val workItem = toBeBatched.poll()
            if (workItem != null) {
                batch.add(workItem)
            } else {
                if ((System.currentTimeMillis() - startBatch) > 10) {
                    return batch
                }
                LockSupport.parkNanos(1_000)
            }
        }
        return batch
    }

    @Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER")
    fun add(element: I): Mono<O> {
        val workToDo = Tuples.of<I, MonoProcessor<O>>(element, MonoProcessor.create())
        if (!toBeBatched.offer(workToDo)) {
            logger().warning("couldnt be processed" + element)
        }
        return workToDo.t2
    }
}