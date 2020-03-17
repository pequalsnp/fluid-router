package com.kylegalloway.fluidrouter.esi

import java.net.URI

import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import spray.json._
import org.apache.http.entity.ContentType

case class Page[T](
  data: T,
  maxPages: Int
)


object ESIUtil {
  def getPage[T](
    uriBuilder: URIBuilder,
    page: Int,
    httpClient: CloseableHttpClient,
  )(implicit r: JsonReader[T]): Page[T] = {
    def uriForPage(page: Int): URI = {
      uriBuilder.setParameter("page", page.toString).build()
    }

    val httpGet = new HttpGet(uriForPage(page))
    val response: CloseableHttpResponse = httpClient.execute(httpGet)
    var maxPages = page
    Option(response.getFirstHeader("x-pages"))
      .foreach{ header =>
        maxPages = header.getValue.toInt
      }
    try {
      val entity = response.getEntity
      if (entity != null) {
        val contentType = ContentType.getOrDefault(entity)
        val charset = contentType.getCharset
        val jsonString = EntityUtils.toString(entity, charset)
        Page(data = jsonString.parseJson.convertTo[T], maxPages = maxPages)
      } else {
        throw deserializationError("Unexpected null page")
      }
    } finally {
      response.close()
    }
  }

  def scanPages[T](
    uriBuilder: URIBuilder,
    httpClient: CloseableHttpClient,
    scanFn: ScanFn[T]
  )(implicit r: JsonReader[T]): Unit = {
    def uriForPage(page: Int): URI = {
      uriBuilder.setParameter("page", page.toString).build()
    }

    var currentPage = 1
    var maxPages = 1
    do {
      val httpGet = new HttpGet(uriForPage(currentPage))
      val response: CloseableHttpResponse = httpClient.execute(httpGet)
      Option(response.getFirstHeader("x-pages"))
        .foreach{ header =>
          maxPages = header.getValue.toInt
        }
      try {
        val entity = response.getEntity
        if (entity != null) {
          val contentType = ContentType.getOrDefault(entity)
          val charset = contentType.getCharset
          val jsonString = EntityUtils.toString(entity, charset)
          val data = jsonString.parseJson.convertTo[T]
          scanFn(currentPage, data, maxPages)
        }
      } finally {
        response.close()
      }
      currentPage += 1
    } while(currentPage <= maxPages)
  }
}
