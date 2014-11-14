package com.chaos.pingplusplus.net

import scala.collection.mutable.Map
/**
 * Created by zcfrank1st on 11/14/14.
 */
class PingppResponse {
  var responseCode: Int = _
  var responseBody: String = _
  var responseHeaders: Map[String, List[String]] = _

  def this(responseCode: Int, responseBody: String) {
    this()
    this.responseCode = responseCode
    this.responseBody = responseBody
    this.responseHeaders = null
  }

  def this(responseCode: Int, responseBody: String, responseHeaders: Map[String, List[String]]) {
    this()
    this.responseCode = responseCode
    this.responseBody = responseBody
    this.responseHeaders = responseHeaders
  }
}
