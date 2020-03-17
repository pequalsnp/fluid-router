package com.kylegalloway.fluidrouter

import com.kylegalloway.fluidrouter.model.{RegionID, TypeID}
import org.apache.http.client.utils.URIBuilder

import scala.util.Try

package object esi {
  val TheForgeRegionID: RegionID = 10000002

  type ScanFn[T] = (Int, T, Int) => Try[Boolean]

  val AllTypesURLPath = "/v1/universe/types/"
  val RegionalMarketURLPattern = "/v1/markets/%d/orders/"

  def ESIURIBuilder(): URIBuilder = {
    new URIBuilder()
      .setScheme("https")
      .setHost("esi.evetech.net")
  }
}
