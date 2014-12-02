package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.Pingpp
import com.chaos.pingplusplus.net.APIResource
import com.chaos.pingplusplus.net.APIResource.RequestMethod

import scala.collection.mutable


/**
 * Created by zcfrank1st on 11/17/14.
 */
class ChargeRefundCollection extends PingppCollectionAPIResource[Refund]{
  def all(params: mutable.HashMap[String, Any]): ChargeRefundCollection = {
    all(params, null)
  }

  def all(params: mutable.HashMap[String, Any], apiKey: String): ChargeRefundCollection = {
    val url: String = String.format("%s%s", Pingpp.apiBase, this.url)
    APIResource.request(RequestMethod.GET, url, params, classOf[ChargeRefundCollection], apiKey)
  }

  def retrieve(id: String): Refund = {
    retrieve(id, null)
  }

  def retrieve(id: String, apiKey: String): Refund = {
    val url: String = String.format("%s%s/%s", Pingpp.apiBase, this.url, id)
    APIResource.request(RequestMethod.GET, url, null, classOf[Refund], apiKey)
  }

  def create(params: mutable.HashMap[String, Any]): Refund = {
    create(params, null)
  }

  def create(params: mutable.HashMap[String, Any], apiKey: String): Refund = {
    val url: String = String.format("%s%s", Pingpp.apiBase, this.url)
    APIResource.request(RequestMethod.POST, url, params, classOf[Refund], apiKey)
  }

  override var obj: String = _
  override var data: List[Refund] = _
  override var url: String = _
  override var hasMore: Boolean = _
}
