package de.trusted.anchor.server.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor

import reactor.core.publisher.UnicastProcessor
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.time.Duration


internal class Batcher<I, O> {
    val processor: UnicastProcessor<Tuple2<I, MonoProcessor<O>>> =
        UnicastProcessor.create<Tuple2<I, MonoProcessor<O>>>()

    fun add(element: I): Mono<O> {
        val workToDo = Tuples.of<I, MonoProcessor<O>>(element, MonoProcessor.create())
        processor.sink().next(workToDo)
        return workToDo.t2
    }

    fun listen(): Flux<List<Tuple2<I, MonoProcessor<O>>>> {
        return processor.bufferTimeout(200, Duration.ofMillis(100))
    }
}