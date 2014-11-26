package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.net.APIResource

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

  def update(params: Map[String, AnyRef]): Refund = {
    update(params, null)
  }

  def update(params: Map[String, AnyRef], apiKey:String) = {
    request() // TODO
    return new Refund()
  }

  def getInstanceURL(): String ={
    if (this.charge != null) {
      return String.format("%s/%s/refunds/%s", classURL(classOf[Charge]), this.charge, this.id)
    }
    null
  }
}
