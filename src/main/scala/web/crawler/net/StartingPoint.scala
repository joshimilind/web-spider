package web.crawler.net
import akka.actor._
//import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import web.crawler.net.ServerActor.{CrawledUrls, StartCrawling}
class Client(StartPoint: ActorRef, url: String, depth: Int) extends Actor {

  /**
    * Initiated url crawling Request by sending StartCrawling
    * message to ServerActor with url to crawl and depth of
    * crawling.
    */
  StartPoint ! StartCrawling(url, depth)

  def receive = {

    /**
      * Printed all crawled links whilst received message
      */
    case CrawledUrls(root, links) =>
      println(
        s"root link: $root \ncrawled with depth: $depth \ncrawled Links: \n${links.toList
          .mkString("\n")}")
  }
}

object Client extends App {
  println("...")
  val _system = ActorSystem("webSpider")
  val StartPoint = _system.actorOf(Props[ServerActor], "webSpider")
  val client = _system.actorOf(
    Props[Client](new Client(StartPoint, "http://www.synerzip.com/", 2)))
}
