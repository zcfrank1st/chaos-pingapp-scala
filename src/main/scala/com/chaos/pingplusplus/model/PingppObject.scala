package com.chaos.pingplusplus.model

import com.google.gson.{FieldNamingPolicy, GsonBuilder}

/**
 * Created by zcfrank1st on 11/14/14.
 */

abstract class PingppObject {
  final var PRETTY_PRINT_GSON = new GsonBuilder().
    setPrettyPrinting().
    serializeNulls().
    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
    create()

  override def toString() = {
    PRETTY_PRINT_GSON.toJson(this)
  }

  def getIdString() = {
    val idField = this.getClass.getDeclaredField("id")
    idField.get(this)
  }
}
