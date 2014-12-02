package com.chaos.pingplusplus.net

import java.io.{UnsupportedEncodingException, InputStream, OutputStream, IOException}
import java.lang.reflect.{Method, Constructor}
import java.net.{URLStreamHandler, URL, HttpURLConnection, URLEncoder}
import java.security.{MessageDigest, NoSuchAlgorithmException}
import java.security.cert.{CertificateEncodingException, Certificate}
import java.util
import java.util.Scanner
import javax.net.ssl.HttpsURLConnection

import com.chaos.pingplusplus.Pingpp
import com.chaos.pingplusplus.exception.{AuthenticationException, APIException, InvalidRequestException, APIConnectionException}
import com.chaos.pingplusplus.model.{PingppRawJsonObjectDeserializer, PingppRawJsonObject, ChargeRefundCollection, PingppObject}
import com.chaos.pingplusplus.net.APIResource.RequestMethod.RequestMethod
import com.google.gson.{FieldNamingPolicy, GsonBuilder, Gson}

import scala.collection.mutable
import scala.collection.JavaConversions._

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

  def singleClassURL(clazz: Class[_]) : String = {
    String.format("%s/v1/%s", Pingpp.apiBase, className(clazz))
  }

  def classUrl(clazz: Class[_]) = {
    String.format("%ss", singleClassURL(clazz))
  }

  def instanceURL(clazz: Class[_], id: String) : String = {
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

  private def createPostConnection (url: String, query: String, apiKey: String): HttpURLConnection =  {
    val conn = createPingppConnection(url, apiKey)
    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", String.format("application/x-www-form-urlencoded;charset=%s",CHARSET))

    checkSSLCert(conn)

    var output: OutputStream = null

    try {
      output = conn.getOutputStream
      output.write(query.toByte)
      conn
    } finally {
      if (output != null) {
        output.close()
      }
    }
  }

  private def createDeleteConnection(url: String, query: String, apiKey: String) = {
    val deleteUrl = formatURL(url, query)
    val conn = createPingppConnection(deleteUrl, apiKey)
    conn.setRequestMethod("DELETE")

    checkSSLCert(conn)
    conn
  }

  private def createQuery(params: mutable.HashMap[String, Any]): String = {
    val flatParams = flattenParams(params)
    val queryStringBuffer: StringBuilder = new StringBuilder
    import scala.collection.JavaConversions._
    for (entry <- flatParams.entrySet) {
      if (queryStringBuffer.length > 0) {
        queryStringBuffer.append("&")
      }
      queryStringBuffer.append(urlEncodePair(entry.getKey, entry.getValue))
    }
    queryStringBuffer.toString
  }

  private def flattenParams(params: mutable.HashMap[String, Any]): mutable.HashMap[String, String] = {
    if (params == null) {
      return new mutable.HashMap[String, String]
    }
    val flatParams: mutable.HashMap[String, String] = new mutable.HashMap[String, String]
    import scala.collection.JavaConversions._
    for (entry <- params.entrySet) {
      val key: String = entry.getKey
      val value: Any = entry.getValue
      if (value.isInstanceOf[Map[_, _]]) {
        val flatNestedMap: mutable.HashMap[String, Any] = new mutable.HashMap[String, Any]
        val nestedMap: Map[_, _] = value.asInstanceOf[Map[_, _]]
        import scala.collection.JavaConversions._
        for (nestedEntry <- nestedMap.entrySet) {
          flatNestedMap.put(String.format("%s[%s]", key, nestedEntry.getKey), nestedEntry.getValue)
        }
        flatParams.putAll(flattenParams(flatNestedMap))
      }
      else if ("" == value) {
        throw new InvalidRequestException("You cannot set '" + key + "' to an empty string. " + "We interpret empty strings as null in requests. " + "You may set '" + key + "' to null to delete the property.", key, null)
      }
      else if (value == null) {
        flatParams.put(key, "")
      }
      else if (value != null) {
        flatParams.put(key, value.toString)
      }
    }
    flatParams
  }

  private class ErrorContainer {
    var error: APIResource.Error = null
  }

  private class Error {
    @SuppressWarnings(Array("unused")) private[net] var `type`: String = null
    private[net] var message: String = null
    private[net] var code: String = null
    private[net] var param: String = null
  }

  private def getResponseBody(responseStream: InputStream): String = {
    val rBody: String = new Scanner(responseStream, CHARSET).useDelimiter("\\A").next
    responseStream.close
    rBody
  }

  private def makeURLConnectionRequest(method: RequestMethod, url: String, query: String, apiKey: String): PingppResponse = {
    var conn: HttpURLConnection = null
    try
      method match {
        case RequestMethod.GET =>
          conn = createGetConnection(url, query, apiKey)
        case RequestMethod.POST =>
          conn = createPostConnection(url, query, apiKey)
        case RequestMethod.DELETE =>
          conn = createDeleteConnection(url, query, apiKey)
        case _ =>
          throw new APIConnectionException(String.format("Unrecognized HTTP method %s. " + "This indicates a bug in the Pingpp bindings. Please contact " + "support@pingplusplus.com for assistance.", method))
      }
      val rCode: Int = conn.getResponseCode
      var rBody: String = null
      var headers: mutable.Map[String, util.List[String]] = null
      if (rCode >= 200 && rCode < 300) {
        rBody = getResponseBody(conn.getInputStream)
      }
      else {
        rBody = getResponseBody(conn.getErrorStream)
      }
      headers = conn.getHeaderFields
      new PingppResponse(rCode, rBody, headers)

    catch {
      case e: IOException => {
        throw new APIConnectionException(String.format("IOException during API request to Pingpp (%s): %s " + "Please check your internet connection and try again. If this problem persists," + "you should check Pingpp's service status at https://pingplusplus.com," + " or let us know at support@pingplusplus.com.", Pingpp.apiBase, e.getMessage), e)
      }
    } finally {
      if (conn != null) {
        conn.disconnect
      }
    }
  }

  def request[T](method: RequestMethod, url: String, params: mutable.HashMap[String, Any], clazz: Class[T], apiKey: String): T = {
    var originalDNSCacheTTL: String = null
    var allowedToSetTTL: Boolean = true
    try {
      originalDNSCacheTTL = java.security.Security.getProperty(DNS_CACHE_TTL_PROPERTY_NAME)
      java.security.Security.setProperty(DNS_CACHE_TTL_PROPERTY_NAME, "0")
    }
    catch {
      case se: SecurityException => {
        allowedToSetTTL = false
      }
    }
    try {
      _request(method, url, params, clazz, apiKey)
    } finally {
      if (allowedToSetTTL) {
        if (originalDNSCacheTTL == null) {
          java.security.Security.setProperty(DNS_CACHE_TTL_PROPERTY_NAME, "-1")
        }
        else {
          java.security.Security.setProperty(DNS_CACHE_TTL_PROPERTY_NAME, originalDNSCacheTTL)
        }
      }
    }
  }

  def _request[T](method: RequestMethod, url: String, params: mutable.HashMap[String, Any], clazz: Class[T], apiKey: String): T = {
    var apiKey = Pingpp.apiKey;
    if ((Pingpp.apiKey == null || Pingpp.apiKey.length == 0) && (apiKey == null || apiKey.length == 0)) {
      throw new AuthenticationException("No API key provided. (HINT: set your API key using 'Pingpp.apiKey = <API-KEY>'. " + "You can generate API keys from the Pingpp web interface. " + "See https://pingplusplus.com for details or email support@pingplusplus.com if you have questions.")
    }
    if (apiKey == null) {
      apiKey = Pingpp.apiKey
    }
    var query: String = null
    try {
      query = createQuery(params)
    }
    catch {
      case e: UnsupportedEncodingException => {
        throw new InvalidRequestException("Unable to encode parameters to " + CHARSET + ". Please contact support@pingplusplus.com for assistance.", null, e)
      }
    }
    var response: PingppResponse = null
    try {
      response = makeURLConnectionRequest(method, url, query, apiKey)
    }
    catch {
      case ce: ClassCastException => {
        val appEngineEnv: String = System.getProperty("com.google.appengine.runtime.environment", null)
        if (appEngineEnv != null) {
          response = makeAppEngineRequest(method, url, query, apiKey)
        }
        else {
          throw ce
        }
      }
    }
    val rCode: Int = response.responseCode
    val rBody: String = response.responseBody
    if (rCode < 200 || rCode >= 300) {
      handleAPIError(rBody, rCode)
    }
    GSON.fromJson(rBody, clazz)
  }

  private def handleAPIError(rBody: String, rCode: Int) {
    val error: APIResource.Error = GSON.fromJson(rBody, classOf[APIResource.ErrorContainer]).error
    rCode match {
      case 400 =>
        throw new InvalidRequestException(error.message, error.param, null)
      case 404 =>
        throw new InvalidRequestException(error.message, error.param, null)
      case 401 =>
        throw new AuthenticationException(error.message)
      case _ =>
        throw new APIException(error.message, null)
    }
  }

  private def makeAppEngineRequest(method: RequestMethod, url: String, query: String, apiKey: String): PingppResponse = {
    val unknownErrorMessage: String = "Sorry, an unknown error occurred while trying to use the " + "Google App Engine runtime. Please contact support@pingplusplus.com for assistance."
    var url = url
    try
      if (method == RequestMethod.GET || method == RequestMethod.DELETE) {
        url = String.format("%s?%s", url, query)
      }
      val fetchURL: URL = new URL(url)
      val requestMethodClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.HTTPMethod")
      val httpMethod: AnyRef = requestMethodClass.getDeclaredField(method.toString).get(null)
      val fetchOptionsBuilderClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.FetchOptions$Builder")
      var fetchOptions: AnyRef = null
      try {
        fetchOptions = fetchOptionsBuilderClass.getDeclaredMethod("validateCertificate").invoke(null)
      }
      catch {
        case e: NoSuchMethodException => {
          System.err.println("Warning: this App Engine SDK version does not allow verification of SSL certificates;" + "this exposes you to a MITM attack. Please upgrade your App Engine SDK to >=1.5.0. " + "If you have questions, contact support@pingplusplus.com.")
          fetchOptions = fetchOptionsBuilderClass.getDeclaredMethod("withDefaults").invoke(null)
        }
      }
      val fetchOptionsClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.FetchOptions")
      fetchOptionsClass.getDeclaredMethod("setDeadline", classOf[Double]).invoke(fetchOptions, 55.toDouble)
      val requestClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.HTTPRequest")
      val request: Any = requestClass.getDeclaredConstructor(classOf[URL], requestMethodClass, fetchOptionsClass).newInstance(fetchURL, httpMethod, fetchOptions)
      if (method == RequestMethod.POST) {
        requestClass.getDeclaredMethod("setPayload", classOf[Array[Byte]]).invoke(request, query.getBytes)
      }
      import scala.collection.JavaConversions._
      for (header <- getHeaders(apiKey).entrySet) {
        val httpHeaderClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.HTTPHeader")
        val reqHeader: Any = httpHeaderClass.getDeclaredConstructor(classOf[String], classOf[String]).newInstance(header.getKey, header.getValue)
        requestClass.getDeclaredMethod("setHeader", httpHeaderClass).invoke(request, reqHeader)
      }
      val urlFetchFactoryClass: Class[_] = Class.forName("com.google.appengine.api.urlfetch.URLFetchServiceFactory")
      val urlFetchService: AnyRef = urlFetchFactoryClass.getDeclaredMethod("getURLFetchService").invoke(null)
      val fetchMethod: Method = urlFetchService.getClass.getDeclaredMethod("fetch", requestClass)
      fetchMethod.setAccessible(true)
      val response: AnyRef = fetchMethod.invoke(urlFetchService, request)
      val responseCode: Int = response.getClass.getDeclaredMethod("getResponseCode").invoke(response).asInstanceOf[Integer]
      val body: String = new String(response.getClass.getDeclaredMethod("getContent").invoke(response).asInstanceOf[Array[Byte]], CHARSET)
      return new PingppResponse(responseCode, body)

    catch {
      case e: _ => {
        throw new APIException(unknownErrorMessage, e)
      }
    }
  }
}