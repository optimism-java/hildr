package io.optimism.driver

import io.optimism.events.Event
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message

/**
 * Created by IntelliJ IDEA.
 * Author: kaichen
 * Date: 2024/7/25
 * Time: 19:46
 */
class StepSchedulingDeriver: Handler<Message<Event>> {
    override fun handle(event: Message<Event>) {
        TODO("Not yet implemented")
    }

}