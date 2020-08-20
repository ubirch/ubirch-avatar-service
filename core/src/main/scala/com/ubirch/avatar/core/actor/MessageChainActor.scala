//package com.ubirch.avatar.core.actor
//
//import akka.actor.{Actor, ActorLogging, ActorRef}
//import com.ubirch.avatar.config.{Config, Const}
//import com.ubirch.avatar.core.device.DeviceManager
//import com.ubirch.avatar.model.db.device.Device
//import com.ubirch.avatar.model.rest.device.DeviceDataRaw
//import com.ubirch.chain.model.rest.{DeviceMsgHashIn, DeviceMsgIn}
//import com.ubirch.util.json.Json4sUtil
//
//import scala.language.postfixOps
//
//
///**
//  * author: derMicha
//  * since: 2016-10-28
//  */
//class MessageChainActor extends Actor
//  with ActorLogging {
//
//  val deviceMsgProducer: ActorRef = context.actorOf(MessageChainProducerActor.props(Config.awsSqsUbirchChainDeviceMsgIn))
//
//  val deviceHashProducer: ActorRef = context.actorOf(MessageChainProducerActor.props(Config.awsSqsUbirchChainDeviceHashIn))
//
//  override def receive: Receive = {
//
//    case (drd: DeviceDataRaw, device: Device) =>
//      log.debug(s"received message: $drd")
//      if (DeviceManager.checkProperty(device, Const.CHAINHASHEDDATA))
//        drd.s match {
//          case Some(s) =>
//            Json4sUtil.any2String(DeviceMsgHashIn(id = drd.id.toString, hash = s)) match {
//              case Some(pStr) =>
//                deviceHashProducer ! pStr
//              case None =>
//                log.error(s"DeviceMsgHashIn could nnot be parsed with: $drd")
//            }
//          case None =>
//            log.error("device data raw was not signed, chaining makes no sense")
//        }
//      else
//        Json4sUtil.any2jvalue(drd) match {
//          case Some(drdJson) =>
//            Json4sUtil.any2String(DeviceMsgIn(id = drd.id.toString, payload = drdJson)) match {
//              case Some(pStr) =>
//                deviceMsgProducer ! pStr
//              case None =>
//                log.error(s"DeviceMsgMsgIn could nnot be parsed with: $drd")
//            }
//          case None =>
//            log.error("device data could not transformed to json string")
//        }
//
//    case _ => log.error("received unknown message")
//
//  }
//
//}
