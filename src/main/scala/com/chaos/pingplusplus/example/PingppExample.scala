import com.chaos.pingplusplus.model.{Channel, Charge}

import scala.collection.mutable

/**
 * Created by zcfrank1st on 11/14/14.
 */
object PingppExample {
  private var chargeID: String = null

  def main(args: Array[String]) {
    charge
  }

  def charge {
    val chargeMap = new mutable.HashMap[String, Any]
    chargeMap.put("amount", 100)
    chargeMap.put("currency", "cny")
    chargeMap.put("subject", "苹果")
    chargeMap.put("body", "一个又大又红的红富士苹果")
    chargeMap.put("amount", 800)
    chargeMap.put("order_no", "1234567890ab")
    chargeMap.put("channel", Channel.WECHAT)
    chargeMap.put("client_ip", "127.0.0.1")
    val app = new mutable.HashMap[String, String]
    app.put("id", "YOUR ID")
    chargeMap.put("app", app)
    try {
      val charge: Charge = Charge.create(chargeMap)
      chargeID = charge.id
      System.out.println(chargeID)
      System.out.println(charge)
      val credential: String = charge.getCredential
      System.out.println(credential)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def refund {
    try {
      val charge: Charge = Charge.retrieve("CHARGE-ID")
      val refundMap = new mutable.HashMap[String, Any]
      refundMap.put("amount", 100)
      refundMap.put("description", "小苹果")
      charge.refund(refundMap)
      System.out.println(charge)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }
}
