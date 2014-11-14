package com.chaos.pingplusplus.exception

/**
 * Created by zcfrank1st on 11/14/14.
 */
class InvalidRequestException(message: String = null, e: Throwable = null) extends PingappException(message, e){
  var param: String = _

  def this(message: String, param:String, e:Throwable) {
    this(message, e)
    this.param = param
  }
}
