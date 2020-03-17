package com.kylegalloway.fluidrouter

import java.time.ZonedDateTime

import com.kylegalloway.fluidrouter.esiasync.RegionalMarketSource
import com.kylegalloway.fluidrouter.mapper.ZonedDateTimeSerializer
import com.kylegalloway.fluidrouter.model.{Order, TypeID}
import com.kylegalloway.fluidrouter.transformations.{MarketBuilder, TypeMarketBuilder}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._

object Job {
  implicit val typeIDTypeInfo = createTypeInformation[TypeID]
  implicit val seqTypeIDTypeInfo = createTypeInformation[Seq[TypeID]]

  def main(args: Array[String]): Unit = {
    // set up the execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment

    env.getConfig.registerTypeWithKryoSerializer(classOf[ZonedDateTime], classOf[ZonedDateTimeSerializer])

    env
      .createInput(RegionalMarketSource())
      .flatMap { orders => orders }
      .groupBy(_.typeID)
      .combineGroup(TypeMarketBuilder())
      .combineGroup(MarketBuilder())

      .print()
  }
}
