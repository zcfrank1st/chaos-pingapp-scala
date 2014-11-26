package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.net.APIResource
import com.google.gson._
import java.lang.reflect.Type
import collection.mutable.Map

/**
 * Created by zcfrank1st on 11/17/14.
 */
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
  var metadata: Map[String, String] = _
  var credential: Map[String, AnyRef] = _
  var description: String = _

  override final val PRETTY_PRINT_GSON = new GsonBuilder().
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

  override def getMetadata(): Map[String, String] = metadata

  override def update(params: Map[String, String]): MetadataStore = {
    update(params, null)
  }

  override def update(params: Map[String, String], apiKey: String): MetadataStore = {

  }

  override def setMetadata(metadata: Map[String, String]): Unit = {
    this.metadata = metadata
  }

  def getCredential: String = {
    val credParams: Map[String, AnyRef] = _
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
 //TODO
}
