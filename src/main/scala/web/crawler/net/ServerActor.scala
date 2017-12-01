package web.crawler.net

import akka.actor.{Actor, Props}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import scala.collection.JavaConversions._

class ServerActor extends Actor {
  def crawl(url: String): Unit = {
    println("\n" + url)
    try {
      val doc = Jsoup.connect(url).get()
      val title = doc.title()
      val links: Elements = doc.select("a[href]")
      for (link <- links) {
        println(s"\ntitle : " + link.text)
        println("link  : " + link.attr("abs:href"))
      }
    } catch {
      case _: Throwable =>
        println(
          "\n Something went wrong, may be web site has disabled robot.txt!")
    }
  }
  def receive: Receive = {
    case url => crawl(s"url:$url")
    case _   => unhandled()
  }
}

object ServerActor {
  def props = Props(classOf[ServerActor])
}
