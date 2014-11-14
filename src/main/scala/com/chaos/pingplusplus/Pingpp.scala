package com.chaos.pingplusplus

/**
 * Created by zcfrank1st on 11/14/14.
 */
object Pingpp {
    final val LIVE_API_BASE: String = "https://api.pingplusplus.com/"
    val VERSION: String = "1.0.3"
    @volatile
    var apiKey: String = null
    @volatile
    var apiVersion: String = "2014-10-10"
    @volatile
    private var verifySSL: Boolean = true
    @volatile
    private var apiBase: String = LIVE_API_BASE
}
