package com.example

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.serialization.Revision
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class AmazonApplication

fun main(args: Array<String>) {
    SpringApplication.run(AmazonApplication::class.java, *args)
}

@Component
class Simulator(val commandGateway: CommandGateway) : CommandLineRunner {
    override fun run(vararg args: String) {
        commandGateway.send<String>(CreateOrder("1234"))
        commandGateway.send<String>(CreateOrder("1235"))
        commandGateway.send<String>(CancelOrder("1234"))
    }
}

data class CreateOrder(@TargetAggregateIdentifier val orderId: String)
@Revision("1.0.0")
data class OrderCreated(val orderId: String)

data class CancelOrder(@TargetAggregateIdentifier val orderId: String)
@Revision("1.0.0")
data class OrderCanceled(val orderId: String)

@Aggregate
class Order {

    @AggregateIdentifier
    private lateinit var id: String

    constructor()

    @CommandHandler
    constructor(cmd: CreateOrder) {
        AggregateLifecycle.apply(OrderCreated(cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: OrderCreated) {
        this.id = event.orderId
    }

    @CommandHandler
    fun cancelOrder(cmd: CancelOrder) {
        AggregateLifecycle.apply(OrderCanceled(cmd.orderId))
    }

}

@Component
class EventLogger {

    @EventHandler
    fun on(event: Any) {
        println(event)
    }
}
