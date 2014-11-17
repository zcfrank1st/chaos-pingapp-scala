package com.chaos.pingplusplus.model

import com.chaos.pingplusplus.net.APIResource

/**
 * Created by zcfrank1st on 11/14/14.
 */
abstract class PingppCollectionAPIResource[T] extends APIResource{
  var obj: String
  var url: String
  var hasMore: Boolean
  var data: List[T]
}
