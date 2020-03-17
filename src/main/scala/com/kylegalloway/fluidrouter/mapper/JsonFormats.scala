package com.kylegalloway.fluidrouter.mapper

import java.time.ZonedDateTime

import com.kylegalloway.fluidrouter.model.{Order, OrderID, TypeID}
import spray.json._

object JsonFormats extends DefaultJsonProtocol {
  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    override def read(json: JsValue): ZonedDateTime = {
      ZonedDateTime.parse(json.convertTo[String])
    }

    override def write(obj: ZonedDateTime): JsValue = {
      JsString(obj.toString)
    }
  }

  private val OrderExpectedFields = Set(
    "type_id",
    "is_buy_order",
    "order_id",
    "issued",
    "min_volume",
    "volume_remain",
    "volume_total",
    "location_id",
    "system_id",
    "range",
    "price"
  )

  implicit object OrderJsonFormat extends RootJsonFormat[Order] {
    override def read(json: JsValue): Order = json match {
      case JsObject(fields) =>
        val missingFields = OrderExpectedFields &~ fields.keySet
        if (missingFields.nonEmpty) {
          throw deserializationError(s"Missing expected fields on Order object from ESI $missingFields")
        }
        Order(
          typeID = fields("type_id").convertTo[TypeID],
          isBuyOrder = fields("is_buy_order").convertTo[Boolean],
          orderID = fields("order_id").convertTo[OrderID],
          issued = fields("issued").convertTo[ZonedDateTime],
          price = fields("price").convertTo[Double],
          minVolume = fields("min_volume").convertTo[Long],
          volumeRemaining = fields("volume_remain").convertTo[Long],
          volumeTotal = fields("volume_total").convertTo[Long],
        )
      case _ => throw deserializationError("Expected Order to be a JSON object")
    }

    override def write(obj: Order): JsValue = {
      JsObject(
        "type_id" -> JsNumber(obj.typeID),
        "is_buy_order" -> JsBoolean(obj.isBuyOrder),
        "order_id" -> JsNumber(obj.orderID),
        "issued" -> obj.issued.toJson,
        "price" -> JsNumber(obj.price),
        "min_volume" -> JsNumber(obj.minVolume),
        "volume_remain" -> JsNumber(obj.volumeRemaining),
        "volume_total" -> JsNumber(obj.volumeTotal)
      )
    }
  }
}
