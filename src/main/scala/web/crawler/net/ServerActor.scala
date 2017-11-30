package web.crawler.net
import akka.actor.{Actor, Props}
import org.jsoup.Jsoup

class ServerActor extends Actor {
//  def props = Props(classOf[ServerActor])

  def crawl(url: String): Unit = {
    println("\n" + url)

    val doc = Jsoup.connect(url).get()
    val title = doc.title()
    val links = doc.select("a[href]")

    println(s"\n \t title :  + $title")
    println(s" \t links :  + $links")
  }
  override def receive: Receive = {
    case url => crawl(s"url:$url")
    case _   => unhandled()
  }

}

object ServerActor {
  def props = Props(classOf[ServerActor])
}
