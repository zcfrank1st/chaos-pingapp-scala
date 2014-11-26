package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.Pingpp

/**
 * Created by zcfrank1st on 11/17/14.
 */
class ChargeRefundCollection extends PingppCollectionAPIResource[Refund]{
  def all (params: Map[String, Any]) : ChargeRefundCollection = {
    all(params, all)
  }

  def all (params: Map[String, Any], apiKey: String) = {
    val url: String = String.format("%s%s", Pingpp.apiKey, this.url)
    request()
  }

  //TODO
}
