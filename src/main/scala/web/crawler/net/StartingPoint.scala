package web.crawler.net
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import web.crawler.net.ServerActor.{CrawledUrls, StartCrawling}
import web.crawler.net.Client._system

class Client(StartPoint: ActorRef, url: String, depth: Int) extends Actor {
  StartPoint ! StartCrawling(url, depth)

  def receive = {
    case CrawledUrls(root, links) =>
      println(s"root link: $root \ncrawled with depth: $depth \ncrawled Links: \n${links.toList.mkString("\n")}")

      println(s"Current Time ${System.currentTimeMillis}")

      Thread.sleep(1000)
      _system.terminate()
  }
}
object Client extends App {

  println(s"Current Time ${System.currentTimeMillis}")
  val _system = ActorSystem("webSpider")
  val StartPoint = _system.actorOf(Props[ServerActor], "webSpider")
  val client = _system.actorOf(
    Props[Client](new Client(StartPoint, "http://www.google.com/", 1)))
}