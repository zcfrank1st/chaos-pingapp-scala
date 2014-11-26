package com.chaos.pingplusplus.net

import java.io.{OutputStream, IOException}
import java.lang.reflect.Constructor
import java.net.{URLStreamHandler, URL, HttpURLConnection, URLEncoder}
import java.security.{MessageDigest, NoSuchAlgorithmException}
import java.security.cert.{CertificateEncodingException, Certificate}
import java.util
import javax.net.ssl.HttpsURLConnection

import com.chaos.pingplusplus.Pingpp
import com.chaos.pingplusplus.exception.APIConnectionException
import com.chaos.pingplusplus.model.{PingppRawJsonObjectDeserializer, PingppRawJsonObject, ChargeRefundCollection, PingppObject}
import com.google.gson.{FieldNamingPolicy, GsonBuilder, Gson}

import scala.collection.mutable

/**
 * Created by zcfrank1st on 11/14/14.
 */
class APIResource extends PingppObject {

}


object APIResource {
  final val GSON: Gson = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .registerTypeAdapter(classOf[ChargeRefundCollection], new ChargeRefundCollection())
    .registerTypeAdapter(classOf[PingppRawJsonObject], new PingppRawJsonObjectDeserializer())
    .create()

  def apply() = {
    new APIResource
  }

  private def className (clazz: Class[_]): String = {
    val className = clazz.getSimpleName.toLowerCase.replace("$"," ")

    if (className == "application") {
      "application_fee"
    } else {
      className
    }
  }

  protected def singleClassURL(clazz: Class[_]) : String = {
    String.format("%s/v1/%s", Pingpp.apiBase, className(clazz))
  }

  protected def classUrl(clazz: Class[_]) = {
    String.format("%ss", singleClassURL(clazz))
  }

  protected def instanceURL(clazz: Class[_], id: String) : String = {
    String.format("%s%s", classUrl(clazz), urlEncode(id))
  }

  final val CHARSET: String = "UTF-8"

  private final val DNS_CACHE_TTL_PROPERTY_NAME: String = "networkaddress.cache.ttl"
  private final val CUSTOM_URL_STREAM_HANDLER_PROPERTY_NAME = "com.pingplusplus.net.customURLStreamHandler"

  object RequestMethod extends Enumeration{
    type RequestMethod = Value
    val GET, POST, DELETE = Value
  }

  private def urlEncode (str: String) = {
    if (str == null) {
      null
    } else {
      URLEncoder.encode(str, CHARSET)
    }
  }

  private def urlEncodePair(k: String, v: String): String = {
    String.format("%s=%s", urlEncode(k), urlEncode(v))
  }

  def getHeaders(apiKey: String): mutable.Map[String, String] = {
    var headers = collection.mutable.Map[String, String]()
    headers += "Accept-Charset" -> CHARSET
    headers += "User-Agent" -> String.format("Pingpp/v1 JavaBindings/%s", Pingpp.VERSION)

    var apiKeyInner:String = ""
    if (apiKey == null) {
      apiKeyInner = Pingpp.apiKey
    }

    headers += "Authorization" -> String.format("Bearer %s", apiKeyInner)

    val propertyNames = Array("os.name", "os.version", "os.arch",
      "java.version", "java.vendor", "java.vm.version",
      "java.vm.vendor")

    var propertyMap = collection.mutable.Map[String, String]()
    for (ele <- propertyNames) {
      propertyMap += ele -> System.getProperty(ele)
    }

    propertyMap += "bindings.version" -> Pingpp.VERSION
    propertyMap += "lang" -> "Scala"
    propertyMap += "publisher" -> "Pingpp"
    headers += "X-Pingpp-Client-User-Agent" -> GSON.toJson(propertyMap)

    if (Pingpp.apiVersion != null) {
      headers += "Pingplusplus-Version" -> Pingpp.apiVersion
    }

    headers
  }

  private def createPingppConnection(url: String, apiKey: String): HttpURLConnection = {
    var pingppURL: URL = null
    val customURLStreamHandlerClassName: String = System.getProperty(CUSTOM_URL_STREAM_HANDLER_PROPERTY_NAME, null)

    if (customURLStreamHandlerClassName != null) {
      try {
        val clazz: Class[URLStreamHandler] = Class.forName(customURLStreamHandlerClassName).asInstanceOf[Class[URLStreamHandler]]
        val constructor: Constructor[URLStreamHandler] = clazz.getConstructor()
        val customHandler: URLStreamHandler = constructor.newInstance()
        pingppURL = new URL(null, url, customHandler)
      } catch {
        case ex: _ => throw new IOException(ex)
      }
    } else {
      pingppURL = new URL(url)
    }

    val conn = pingppURL.openConnection().asInstanceOf[HttpURLConnection]
    conn.setConnectTimeout(30 * 1000)
    conn.setReadTimeout(80 * 1000)
    conn.setUseCaches(false)

    for ((k, v) <- getHeaders(apiKey)) {
      conn.setRequestProperty(k, v)
    }

    conn
  }

  private def throwInvalidCertificateException(): Unit = {
    throw new APIConnectionException("Invalid server certificate. You tried to connect to a server that has a revoked SSL certificate, which means we cannot securely send data to that server. Please email support@pingplusplus.com if you need help connecting to the correct API server.")
  }


  private def checkSSLCert (hconn: HttpURLConnection): Unit = {
    if (!Pingpp.verifySSL && !(hconn.getURL.getHost == "api.pingplusplus.com")) {
      return
    }

    val conn: HttpsURLConnection = hconn.asInstanceOf[HttpsURLConnection]
    conn.connect()

    val certs: Array[Certificate] = conn.getServerCertificates

    try {
      val md: MessageDigest = MessageDigest.getInstance("SHA-1")
      val der: Array[Byte] = certs(0).getEncoded
      md.update(der)

      val digest = md.digest()

      val revokeCertDigest = Array(0x05.toByte, 0xc0.toByte, 0xb3.toByte, 0x64.toByte, 0x36.toByte, 0x94.toByte, 0x47.toByte, 0x0a.toByte, 0x88.toByte, 0x8c.toByte, 0x6e.toByte, 0x7f.toByte, 0xeb.toByte, 0x5c.toByte, 0x9e.toByte, 0x24.toByte, 0xe8.toByte, 0x23.toByte, 0xdc.toByte, 0x53.toByte);

      if (util.Arrays.equals(digest, revokeCertDigest)) {
        throwInvalidCertificateException()
      }
    } catch {
      case ex: NoSuchAlgorithmException => throw new RuntimeException(ex)
      case ex: CertificateEncodingException => throwInvalidCertificateException()
    }
  }

  private def formatURL (url: String, query: String): String = {
    if (query == null || query.isEmpty) {
      url
    } else {
      val separator = url.contains("?") match {
        case true => "&"
        case false => "?"
      }
      String.format("%s%s%s", url, separator, query)
    }
  }

  private def createGetConnection (url: String, query: String, apiKey: String) = {
    val getUrl = formatURL(url, query)
    val conn = createPingppConnection(url, apiKey)
    conn.setRequestMethod("GET")
    checkSSLCert(conn)
    conn
  }

  private def createPostConnection (url: String, query: String, apiKey: String) = {
    val conn = createPingppConnection(url, apiKey)
    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded;charset=%s",CHARSET))

    checkSSLCert(conn)

    var output: OutputStream = null

    try {
      output = conn.getOutputStream
      output.write(query.toByte)
    } finally {
      if (output != null) {
        output.close()
      }
      conn
    }
  }


  // TODO
}