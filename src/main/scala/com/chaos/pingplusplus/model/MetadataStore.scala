package com.chaos.pingplusplus.model

/**
 * Created by zcfrank1st on 11/14/14.
 */
trait MetadataStore[T] {
  def getMetadata(): Map[String, String]

  def setMetadata(metadata: Map[String,String])

  def update(params: Map[String, String]): MetadataStore

  def update(params: Map[String, String], apiKey: String): MetadataStore
}
