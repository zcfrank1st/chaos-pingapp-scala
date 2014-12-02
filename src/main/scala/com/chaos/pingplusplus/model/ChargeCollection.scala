package com.chaos.pingplusplus.model

/**
 * Created by zcfrank1st on 11/17/14.
 */
class ChargeCollection extends PingppCollection[Charge]{
  override var obj: String = _
  override var data: List[Charge] = _
  override var url: String = _
  override var hasMore: Boolean = _
}
