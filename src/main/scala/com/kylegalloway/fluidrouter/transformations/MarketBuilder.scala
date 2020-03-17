package com.kylegalloway.fluidrouter.transformations

import java.lang

import com.kylegalloway.fluidrouter.model.{Order, TypeID, TypeMarket}
import org.apache.flink.api.common.functions.GroupCombineFunction
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._

object MarketBuilder {
  def apply(): MarketBuilder = {
    new MarketBuilder
  }
}

class MarketBuilder extends GroupCombineFunction[TypeMarket, Map[TypeID, TypeMarket]] {
  override def combine(values: lang.Iterable[TypeMarket], out: Collector[Map[TypeID, TypeMarket]]): Unit = {
    val market = values.asScala.map { typeMarket => typeMarket.typeID -> typeMarket}.toMap
    out.collect(market)
  }
}

object TypeMarketBuilder {
  def apply(): TypeMarketBuilder = {
    new TypeMarketBuilder
  }
}

class TypeMarketBuilder extends GroupCombineFunction[Order, TypeMarket] {
  override def combine(values: lang.Iterable[Order], out: Collector[TypeMarket]): Unit = {
    val typeMarket =
      values.asScala.foldLeft(TypeMarket(typeID = 0, bid = Double.MinPositiveValue, ask = Double.MaxValue)) {
        (market, order) =>
          if (order.isBuyOrder) {
            TypeMarket(
              typeID = order.typeID,
              bid = Math.max(market.bid, order.price),
              ask = market.ask
            )
          } else {
            TypeMarket(
              typeID = order.typeID,
              bid = market.bid,
              ask = Math.min(market.ask, order.price)
            )
          }
      }
    out.collect(typeMarket)
  }
}
