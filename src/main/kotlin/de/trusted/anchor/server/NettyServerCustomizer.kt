package de.trusted.anchor.server

import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelOption
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component
import reactor.netty.http.server.HttpServer

@Component
class MyNettyWebServerCustomizer : WebServerFactoryCustomizer<NettyReactiveWebServerFactory?> {
    override fun customize(factory: NettyReactiveWebServerFactory?) {
        factory?.addServerCustomizers(TcpNettyCustomizer())
    }
}

internal class TcpNettyCustomizer : NettyServerCustomizer {
    override fun apply(httpServer: HttpServer): HttpServer {
        return httpServer.tcpConfiguration({ tcpServer ->
            tcpServer
                .selectorOption(ChannelOption.SO_BACKLOG, 65535)
                .selectorOption(ChannelOption.SO_REUSEADDR, true)
                .selectorOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        })
    }
}