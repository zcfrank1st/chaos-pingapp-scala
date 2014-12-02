package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.net.APIResource
import com.chaos.pingplusplus.net.APIResource.RequestMethod

import scala.collection.mutable

/**
 * Created by zcfrank1st on 11/17/14.
 */
class Refund extends APIResource{
  var id :String = _
  var obj: String = _
  var orderNo: String = _
  var amount: Int = _
  var created: Long = _
  var succeed: Boolean = _
  var timeSucceed: Long = _
  var description: String = _
  var failureCode: String = _
  var failureMsg: String = _
  var metadata: Map[String, String] = _
  var charge: String = _

  def update(params: mutable.HashMap[String, Any]): Refund = {
    update(params, null)
  }

  def update(params: mutable.HashMap[String, Any], apiKey:String): Refund = {
    APIResource.request(RequestMethod.POST, this getInstanceURL(), params, classOf[Refund], apiKey )
  }

  def getInstanceURL(): String ={
    if (this.charge != null) {
      return String.format("%s/%s/refunds/%s", APIResource.classUrl(Class[Charge]), this.charge, this.id)
    }
    null
  }
}
