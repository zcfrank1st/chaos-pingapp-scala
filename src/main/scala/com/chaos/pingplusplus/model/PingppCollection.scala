package com.chaos.pingplusplus.model

/**
 * Created by zcfrank1st on 11/14/14.
 */
abstract class PingppCollection[T] extends PingppObject{
  var obj: String
  var url: String
  var hasMore: Boolean
  var data: List[T]
}
