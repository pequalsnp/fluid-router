package com.kylegalloway.fluidrouter.model

import java.time.ZonedDateTime

case class Type(
  typeID: TypeID
)

case class Order(
  orderID: OrderID,
  typeID: TypeID,
  issued: ZonedDateTime,
  price: Double,
  isBuyOrder: Boolean,
  minVolume: Long,
  volumeRemaining: Long,
  volumeTotal: Long,
)
