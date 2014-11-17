package com.chaos.pingplusplus.net

import com.chaos.pingplusplus.model.PingppObject
import com.google.gson.{FieldNamingPolicy, GsonBuilder, Gson}

/**
 * Created by zcfrank1st on 11/14/14.
 */
abstract class APIResource extends PingppObject{
  final val gson: Gson = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .registerTypeAdapter() //TODO
}
