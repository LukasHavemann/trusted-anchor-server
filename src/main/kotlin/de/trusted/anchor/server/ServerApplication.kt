package de.trusted.anchor.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServerApplication

fun main(args: Array<String>) {
    // BlockHound.install();
    runApplication<ServerApplication>(*args)
}