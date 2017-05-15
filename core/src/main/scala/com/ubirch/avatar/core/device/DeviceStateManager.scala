package com.ubirch.avatar.core.device

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESBulkStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil

import org.json4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by derMicha on 09/11/16.
  */
case class SimplePayLoad(i: Int = 900)

object DeviceStateManager extends MyJsonProtocol with StrictLogging {

  private val index = Config.esDeviceStateIndex
  private val esType = Config.esDeviceStateType

  def currentDeviceState(device: Device): DeviceStateUpdate = {

    val payload = device.deviceConfig.getOrElse(Json4sUtil.any2jvalue(SimplePayLoad()).get)

    val (k, s) = DeviceUtil.sign(payload, device)

    DeviceStateUpdate(
      id = UUIDUtil.uuid,
      k = k,
      s = s,
      p = payload
    )
  }

  /**
    * Insert or update an [[DeviceStateUpdate]].
    *
    * @param state object to upsert
    * @return json of what we stored; None if something went wrong
    */
  def upsert(state: DeviceStateUpdate): Future[Option[DeviceStateUpdate]] = {

    Json4sUtil.any2jvalue(state) match {

      case Some(doc) =>

        val id = state.id.toString
        ESBulkStorage.storeDocBulk(
          docIndex = index,
          docType = esType,
          docId = id,
          doc = doc
        ) map (_.extractOpt[DeviceStateUpdate])

      case None => Future(None)

    }
  }
}
