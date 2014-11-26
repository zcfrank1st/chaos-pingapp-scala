package com.chaos.pingplusplus.model

import com.google.gson.Gson

/**
 * Created by zcfrank1st on 11/17/14.
 */
object Notify {
  class InnerObj {
    var obj: String = _
  }

  def parseNotify(notifyJson: String): Any = {
    var innerObj: InnerObj = new InnerObj
    innerObj = new Gson().fromJson(notifyJson, innerObj.getClass)
    if (innerObj == null || innerObj.obj == null || innerObj.obj.isEmpty) return null

    if (innerObj.obj == "charge") {
      return new Gson().fromJson(notifyJson, classOf[Charge])
    }
    else if (innerObj.obj == "refund") {
      return new Gson().fromJson(notifyJson, classOf[Refund])
    }

    null
  }
}
