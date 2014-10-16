package coinffeine.peer.exchange.micropayment

import scala.concurrent.duration._

import akka.testkit._
import org.joda.time.DateTime

import coinffeine.model.currency._
import coinffeine.model.payment.Payment
import coinffeine.peer.ProtocolConstants
import coinffeine.peer.exchange.micropayment.MicroPaymentChannelActor._
import coinffeine.peer.payment.PaymentProcessorActor
import coinffeine.protocol.messages.exchange.{PaymentProof, StepSignatures}

class HappyPathBuyerMicroPaymentChannelActorTest extends BuyerMicroPaymentChannelActorTest {

  override def protocolConstants =
    ProtocolConstants(microPaymentChannelResubmitTimeout = 2.seconds.dilated)

  "The buyer exchange actor" should "subscribe to the relevant messages when initialized" in {
    gateway.expectNoMsg(100.millis.dilated)
    gateway.expectSubscription()
  }

  it should "respond to step signature messages by sending a payment until all steps are done" in {
    for (i <- 1 to lastStep) withClue(s"At step $i:") {
      actor ! fromCounterpart(StepSignatures(exchange.id, i, signatures))
      listener.expectMsgType[LastBroadcastableOffer]
      expectProgress(signatures = i)
      paymentProcessor.expectMsgType[PaymentProcessorActor.Pay[_]]
      paymentProcessor.reply(PaymentProcessorActor.Paid(
        Payment(s"payment$i", "sender", "receiver", 1.EUR, DateTime.now(), "description",
          completed = true)
      ))
      expectProgress(signatures = i)
      gateway.expectForwarding(PaymentProof(exchange.id, s"payment$i", i), counterpartId)
      gateway.expectNoMsg(100.millis.dilated)
    }
  }

  it should "resubmit payment proof when no response is get" in {
    gateway.expectForwarding(PaymentProof(exchange.id, s"payment$lastStep", lastStep),
      counterpartId)
  }

  it should "send a notification to the listeners once the exchange has finished" in {
    actor ! fromCounterpart(
      StepSignatures(exchange.id, exchange.amounts.breakdown.totalSteps, signatures))
    listener.expectMsgType[LastBroadcastableOffer]
    listener.expectMsg(ChannelSuccess(Some(expectedLastOffer)))
  }
}
