package com.kylegalloway.fluidrouter.esiasync

import com.kylegalloway.fluidrouter.esi
import com.kylegalloway.fluidrouter.model.{Order, RegionID}
import org.apache.flink.api.common.io.statistics.BaseStatistics
import org.apache.flink.api.common.io.{DefaultInputSplitAssigner, InputFormat}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.io.{InputSplit, InputSplitAssigner}
import org.apache.http.impl.client.HttpClients
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

case class RegionSplit(splitNum: Int, regionID: RegionID) extends InputSplit {
  override val getSplitNumber: Int = splitNum
}

object RegionalMarketSource {
  def apply(): RegionalMarketSource = {
    new RegionalMarketSource
  }
}

class RegionalMarketSource(
) extends InputFormat[Seq[Order], RegionSplit] {
  import com.kylegalloway.fluidrouter.mapper.JsonFormats._

  val LOG = LoggerFactory.getLogger(classOf[RegionalMarketSource])

  var currentRegionID: RegionID = 0
  var page: Int = 1
  var maxPages: Int = Int.MaxValue

  override def configure(parameters: Configuration): Unit = {
    LOG.error(s"configuration $parameters")
  }

  override def createInputSplits(minNumSplits: Int): Array[RegionSplit] = {
    Array[RegionSplit](RegionSplit(0, esi.TheForgeRegionID))
  }

  override def getInputSplitAssigner(inputSplits: Array[RegionSplit]): InputSplitAssigner = {
    return new DefaultInputSplitAssigner(inputSplits.toSeq.asJavaCollection)
  }

  override def getStatistics(cachedStatistics: BaseStatistics): BaseStatistics = {
    return cachedStatistics
  }

  override def open(split: RegionSplit): Unit = {
    LOG.error(s"Opening split $split")
    currentRegionID = split.regionID
    page = 1
  }

  override def close(): Unit = {
    LOG.debug(s"Closing region $currentRegionID next page $page maxPages $maxPages")
    page = 1
  }

  override def nextRecord(reuse: Seq[Order]): Seq[Order] = {
    LOG.debug(s"Fetching region $currentRegionID page $page maxPages $maxPages")
    val pageData = esi.ESIUtil.getPage[Seq[Order]](
      esi.ESIURIBuilder().setPath(esi.RegionalMarketURLPattern.format(currentRegionID)),
      page,
      HttpClients.createDefault()
    )
    page += 1
    maxPages = pageData.maxPages
    pageData.data
  }

  override def reachedEnd(): Boolean = page > maxPages
}
