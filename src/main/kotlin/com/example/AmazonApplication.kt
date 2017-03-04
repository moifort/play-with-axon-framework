package com.example

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.commandhandling.model.AggregateIdentifier
import org.axonframework.commandhandling.model.AggregateLifecycle
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.Snapshotter
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.serialization.Revision
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotterFactoryBean
import org.axonframework.spring.stereotype.Aggregate
import org.h2.server.web.WebServlet
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


@SpringBootApplication
class AmazonApplication {

    @Bean
    fun h2servletRegistration(): ServletRegistrationBean {
        val registration = ServletRegistrationBean(WebServlet(), "/console/*")
        return registration
    }

    @Bean("orderRepository")
    fun orderRepository(eventStore: EventStore, snapshotter: Snapshotter) =
            EventSourcingRepository(
                    Order::class.java,
                    eventStore,
                    EventCountSnapshotTriggerDefinition(snapshotter, 3))

//    @Bean("orderRepository")
//    fun orderRepository(eventStore: EventStore) =
//            CachingEventSourcingRepository(
//                    GenericAggregateFactory(Order::class.java),
//                    eventStore,
//                    EhCacheAdapter(Cache(CacheConfiguration("orderCache", 1000))))

    @Bean
    fun snapshotterFactoryBean() = SpringAggregateSnapshotterFactoryBean()
}

fun main(args: Array<String>) {
    SpringApplication.run(AmazonApplication::class.java, *args)
}

@Component
class Simulator(val commandGateway: CommandGateway) : CommandLineRunner {
    override fun run(vararg args: String) {
        println("-----> Create Order")
        commandGateway.send<String>(CreateOrder("1234"))
        println("-----> Update Order")
        commandGateway.send<String>(UpdateOrder("1234"))
        println("-----> Cancel Order")
        commandGateway.send<String>(CancelOrder("1234"))
        println("-----> Update Order")
        commandGateway.send<String>(UpdateOrder("1234"))
        println("-----> Cancel Order")
        commandGateway.send<String>(CancelOrder("1234"))
//        println("-----> Update Order")
//        commandGateway.send<String>(UpdateOrder("1234"))
//        println("-----> Cancel Order")
//        commandGateway.send<String>(CancelOrder("1234"))
//        println("-----> Update Order")
//        commandGateway.send<String>(UpdateOrder("1234"))
//        println("-----> Cancel Order")
//        commandGateway.send<String>(CancelOrder("1234"))
    }
}

data class CreateOrder(@TargetAggregateIdentifier val orderId: String)
@Revision("1.0.0")
data class OrderCreated(val orderId: String)

data class UpdateOrder(@TargetAggregateIdentifier val orderId: String)
@Revision("1.0.0")
data class OrderUpdated(val orderId: String)

data class CancelOrder(@TargetAggregateIdentifier val orderId: String)
@Revision("1.0.0")
data class OrderCanceled(val orderId: String)

@Aggregate
class Order {

    @AggregateIdentifier
    lateinit var id: String
    lateinit var status: String

    constructor()

    @CommandHandler
    constructor(cmd: CreateOrder) {
        AggregateLifecycle.apply(OrderCreated(cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: OrderCreated) {
        println("Aggregate $event - $this")
        id = event.orderId
        status = "CREATED"
    }

    @CommandHandler
    fun udapteOrder(cmd: UpdateOrder) {
        AggregateLifecycle.apply(OrderUpdated(cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: OrderUpdated) {
        status = "UPDATED"
        println("Aggregate $event - $this")
    }

    @CommandHandler
    fun cancelOrder(cmd: CancelOrder) {
        AggregateLifecycle.apply(OrderCanceled(cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: OrderCanceled) {
        status = "CANCELED"
        println("Aggregate $event - $this")
    }
}


data class CreateShipment(@TargetAggregateIdentifier val shipmentId: String, val orderId: String)
@Revision("1.0.0")
data class ShipmentCreated(val shipmentId: String, val orderId: String)

data class CancelShipment(@TargetAggregateIdentifier val shipmentId: String)
@Revision("1.0.0")
data class ShipmentCanceled(val shipmentId: String)

@Aggregate
class Shipment {

    @AggregateIdentifier
    lateinit var id: String

    constructor()

    @CommandHandler
    constructor(cmd: CreateShipment) {
        AggregateLifecycle.apply(ShipmentCreated(cmd.shipmentId, cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: ShipmentCreated) {
        println("Aggregate $event")
        this.id = event.shipmentId
    }

    @CommandHandler
    fun cancelOrder(cmd: CancelShipment) {
        AggregateLifecycle.apply(ShipmentCanceled(cmd.shipmentId))
    }

    @EventSourcingHandler
    fun on(event: ShipmentCanceled) {
        println("Aggregate $event")
    }
}

data class CreatePayment(@TargetAggregateIdentifier val paymentId: String, val orderId: String)
@Revision("1.0.0")
data class PaymentCreated(val paymentId: String, val orderId: String)

data class CancelPayment(@TargetAggregateIdentifier val paymentId: String)
@Revision("1.0.0")
data class PaymentCanceled(val paymentId: String)

@Aggregate
class Payment {

    @AggregateIdentifier
    lateinit var id: String

    constructor()

    @CommandHandler
    constructor(cmd: CreatePayment) {
        AggregateLifecycle.apply(PaymentCreated(cmd.paymentId, cmd.orderId))
    }

    @EventSourcingHandler
    fun on(event: PaymentCreated) {
        println("Aggregate $event")
        this.id = event.paymentId
    }

    @CommandHandler
    fun cancelPayment(cmd: CancelPayment) {
        AggregateLifecycle.apply(PaymentCanceled(cmd.paymentId))
    }

    @EventSourcingHandler
    fun on(event: PaymentCanceled) {
        println("Aggregate $event")
    }
}


//@Saga
//class OrderManagment {
//    lateinit var orderId: String
//    lateinit var shipmentId: String
//    lateinit var paymentId: String
//
//    @StartSaga
//    @SagaEventHandler(associationProperty = "orderId")
//    fun handle(event: OrderCreated, commandGateway: CommandGateway) {
//        this.orderId = event.orderId
//        this.shipmentId = UUID.randomUUID().toString()
//        this.paymentId = UUID.randomUUID().toString()
//        commandGateway.send<String>(CreateShipment(this.shipmentId, this.orderId))
//        commandGateway.send<String>(CreatePayment(this.paymentId, this.orderId))
//    }
//
//    @EndSaga
//    @SagaEventHandler(associationProperty = "orderId")
//    fun handle(event: OrderCanceled, commandGateway: CommandGateway) {
//        commandGateway.send<String>(CancelShipment(this.shipmentId))
//        commandGateway.send<String>(CancelPayment(this.paymentId))
//    }
//}

@Component
class EventLogger {

    @EventHandler
    fun on(event: Any) {
        //println("Event $event")
    }
}

