package io.optimism.driver

import io.optimism.config.Config
import io.optimism.events.Event
import io.optimism.network.ExecutionPayloadEnvelop
import io.optimism.type.L1BlockRef
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.selectUnbiased

/**
 * Created by IntelliJ IDEA.
 * Author: kaichen
 * Date: 2024/7/24
 * Time: 20:59
 */
class NewDriver(
    private val unsafeL2Payloads: Channel<ExecutionPayloadEnvelop> = Channel(10),
    private val l1HeadSignal: Channel<L1BlockRef> = Channel(10),
    private val l1SafeSignal: Channel<L1BlockRef> = Channel(10),
    private val l1FinalizedSignal: Channel<L1BlockRef> = Channel(10),
    private val config: Config,
) : AbstractVerticle(), Handler<Message<Event?>?> {

    override fun start() {

    }

    @ObsoleteCoroutinesApi
    suspend fun eventLoop() {
        val altSyncTicker = ticker(delayMillis = config.chainConfig.blockTime.toLong() * 1000L * 2L)
        while (true) {
            selectUnbiased {
                unsafeL2Payloads.onReceiveCatching {
                    println("New driver received unsafe L2 payloads")
                }
                altSyncTicker.onReceiveCatching {
                    println("New driver received alt sync ticker")
                }
                l1HeadSignal.onReceiveCatching {
                    println("New driver received L1 head signal")
                }
                l1SafeSignal.onReceiveCatching {
                    println("New driver received L1 safe signal")
                }
                l1FinalizedSignal.onReceiveCatching {
                    println("New driver received L1 finalized signal")
                }
            }
        }
        altSyncTicker.cancel()
    }

    override fun stop() {
        println("New driver stopped")
    }

    override fun handle(event: Message<Event?>?) {
    }
}
