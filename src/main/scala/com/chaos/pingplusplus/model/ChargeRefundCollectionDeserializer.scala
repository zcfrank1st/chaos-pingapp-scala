package com.chaos.pingplusplus.model

import java.lang.reflect.Type

import com.google.gson._
import com.google.gson.reflect.TypeToken

/**
 * Created by zcfrank1st on 11/17/14.
 */
class ChargeRefundCollectionDeserializer extends JsonDeserializer[ChargeRefundCollection] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChargeRefundCollection = {
    val gson: Gson = new GsonBuilder().
      setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
      create()

    if (json.isJsonArray) {
      val refundListType: Type = new TypeToken[List[Refund]](){}.getType
      val refunds = gson.fromJson(json, refundListType)
      val collection = new ChargeRefundCollection
      collection.data = refunds
      collection.hasMore = false
      return collection
    }

    gson.fromJson(json, typeOfT)
  }
}
