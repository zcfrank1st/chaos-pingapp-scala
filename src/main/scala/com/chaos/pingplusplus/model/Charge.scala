package com.chaos.pingplusplus.model

import java.lang.reflect.Type

import com.chaos.pingplusplus.net.APIResource
import com.chaos.pingplusplus.net.APIResource.RequestMethod
import com.google.gson._

import scala.collection.mutable
/**
 * Created by zcfrank1st on 11/17/14.
 */
object Charge {
  def create(params: mutable.HashMap[String, Any]): Charge = {
    create(params, null)
  }

  def retrieve(id: String): Charge = {
    retrieve(id, null)
  }

  def all(params: mutable.HashMap[String, Any]): ChargeCollection = {
    all(params, null)
  }

  def create(params: mutable.HashMap[String, Any], apiKey: String): Charge = {
    APIResource.request(RequestMethod.POST, APIResource.classUrl(classOf[Charge]), params, classOf[Charge], apiKey)
  }

  def retrieve(id: String, apiKey: String): Charge = {
    APIResource.request(RequestMethod.GET, APIResource.instanceURL(classOf[Charge], id), null, classOf[Charge], apiKey)
  }

  def all(params: mutable.HashMap[String, Any], apiKey: String): ChargeCollection = {
    APIResource.request(RequestMethod.GET, APIResource.classUrl(classOf[Charge]), params, classOf[ChargeCollection], apiKey)
  }
}

class Charge extends APIResource with MetadataStore[Charge] {
  var id: String = _
  var obj: String = _
  var created: Long = _
  var livemode: Boolean = _
  var paid: Boolean = _
  var refunded: Boolean = _
  var app: String = _
  var channel: String = _
  var orderNo: String = _
  var clientIp: String = _
  var amount: Integer = _
  var amountSettle: Integer = _
  var currency: String = _
  var subject: String = _
  var body: String = _
  var timeExpire: Long = _
  var timeSettle: Long = _
  var transactionNo: String = _
  var refunds: ChargeRefundCollection = _
  var amountRefunded: Integer = _
  var failureCode: String = _
  var failureMsg: String = _
  var metadata: mutable.HashMap[String, String] = _
  var credential: Map[String, AnyRef] = _
  var description: String = _

  override val PRETTY_PRINT_GSON = new GsonBuilder().
    setPrettyPrinting().
    serializeNulls().
    disableHtmlEscaping().
    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
    setLongSerializationPolicy(LongSerializationPolicy.STRING).
    registerTypeAdapter(classOf[Double], new JsonSerializer[Double]() {
    def serialize(src: Double, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      if (src == src.longValue) return new JsonPrimitive(src.longValue)
      return new JsonPrimitive(src)
    }
  }).
    create()

  def getRefunds(): ChargeRefundCollection = {
    if (refunds.url == null) {
      refunds.url = String.format("/v1/charges/%s/refunds", id)
    }
    refunds
  }

  def getCredential: String = {
    val credParams = new mutable.HashMap[String, Any]
    if (channel == Channel.UPMP) {
      credParams.put(channel, credential.get(channel))
    }
    else if (channel == Channel.WECHAT) {
      credParams.put(channel, credential.get(channel))
    }
    else if (channel == Channel.ALIPAY) {
      credParams.put(channel, credential.get(channel))
    }
    PRETTY_PRINT_GSON.toJson(credParams)
  }



  def update(params: mutable.HashMap[String, Any]) = {
    update(params, null)
  }


  def refund: Charge = {
    this.refund(null, null)
  }

  def refund(params: mutable.HashMap[String, Any]): Charge = {
    this.refund(params, null)
  }

  def update(params: mutable.HashMap[String, Any], apiKey: String) = {
    APIResource.request(RequestMethod.POST, APIResource.instanceURL(classOf[Charge], id), params, classOf[Charge], apiKey)
  }

  def refund(apiKey: String): Charge = {
    this.refund(null.asInstanceOf[mutable.HashMap[String, Any]], apiKey)
  }

  def refund(params: mutable.HashMap[String, Any], apiKey: String): Charge = {
    APIResource.request(RequestMethod.POST, String.format("%s/refunds", APIResource.instanceURL(classOf[Charge], this.id)), params, classOf[Charge], apiKey)
  }

  override def getMetadata(): mutable.HashMap[String, String] = metadata

  override def setMetadata(metadata: mutable.HashMap[String, String]): Unit = {
    this.metadata = metadata
  }

}
