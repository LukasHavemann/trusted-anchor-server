@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package de.trusted.anchor.server.service.publication

import de.trusted.anchor.server.service.Loggable
import de.trusted.anchor.server.service.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct


@Service
class EtherumPublishProvider : Loggable, PublishProvider {

    @Value("\${trustedanchor.ethereum.gasLimit:1000000}")
    private lateinit var gasLimit: java.lang.Long

    @Value("\${trustedanchor.ethereum.gasPrice:100}")
    private lateinit var gasPrice: java.lang.Long

    @Value("\${trustedanchor.ethereum.nonce}")
    private lateinit var nonce: java.lang.Long

    @Value("\${trustedanchor.ethereum.senderAccount}")
    private lateinit var senderAccount: String

    @Value("\${trustedanchor.ethereum.senderAccount}")
    private lateinit var contractAccount: String

    @Value("\${trustedanchor.ethereum.url}")
    private lateinit var url: String

    private lateinit var service: Web3jService
    private lateinit var web3j: Web3j
    val counter = AtomicLong()

    @PostConstruct
    fun init() {
        service = HttpService(url)
        web3j = Web3j.build(service)
        counter.set(nonce.toLong())
    }

    fun sendTransaction(hash: String, cid: String) {
        val inputParams: MutableList<Type<*>> = Arrays.asList(Utf8String(hash) as Type<*>, Utf8String(cid) as Type<*>)
        val outputParams: List<TypeReference<*>> = Collections.emptyList()

        val function = Function("log", inputParams, outputParams)
        val encodedFunction = FunctionEncoder.encode(function)

        val transaction: Transaction = Transaction
            .createFunctionCallTransaction(
                senderAccount,
                nextNonce(),
                BigInteger.valueOf(gasPrice.toLong()),
                BigInteger.valueOf(gasLimit.toLong()),
                contractAccount,
                encodedFunction
            )

        val transactionResponse = web3j.ethSendTransaction(transaction).sendAsync().get()
        logger().info("publish hash ${hash} with cid ${cid} has result ${transactionResponse}")
    }

    private fun nextNonce(): BigInteger {
        return BigInteger.valueOf(counter.incrementAndGet())
    }

    override fun publish(hash: ByteArray, cid: String) {
        TODO("Not yet implemented")
    }
}