package com.chaos.pingplusplus.model

import java.lang.reflect.Type

import com.google.gson.{JsonDeserializationContext, JsonElement, JsonDeserializer}

/**
 * Created by zcfrank1st on 11/17/14.
 */
class PingppRawJsonObjectDeserializer extends JsonDeserializer[PingppRawJsonObject]{
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PingppRawJsonObject = {
    val obj: PingppRawJsonObject = new PingppRawJsonObject
    obj.json = json.getAsJsonObject
    obj
  }
}
