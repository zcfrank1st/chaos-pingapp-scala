package com.chaos.pingplusplus.model

import scala.collection.mutable

/**
 * Created by zcfrank1st on 11/14/14.
 */
trait MetadataStore[T] {
  def getMetadata(): mutable.HashMap[String, String]

  def setMetadata(metadata: mutable.HashMap[String,String])

  def update(params: mutable.HashMap[String, Any])

  def update(params: mutable.HashMap[String, Any], apiKey: String)
}
