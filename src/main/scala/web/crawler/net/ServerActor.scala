package web.crawler.net

import akka.actor._
import web.crawler.net.ServerActor.{StartCrawling, CrawledUrls}
import web.crawler.net.LinkChecker.Result
import scala.collection.mutable
object ServerActor {
  case class StartCrawling(url: String, depth: Int) {}
  case class CrawledUrls(url: String, links: Set[String]) {}
}
class ServerActor extends Actor {

  val clients: mutable.Map[String, Set[ActorRef]] =
    collection.mutable.Map[String, Set[ActorRef]]()
  val linkcontroller: mutable.Map[String, ActorRef] =
    mutable.Map[String, ActorRef]()

  def receive = {

    case StartCrawling(url, depth) =>
      val controller = linkcontroller get url
      if (controller.isEmpty) {
        linkcontroller += (url -> context.actorOf(
          Props[LinkChecker](new LinkChecker(url, depth))))
        clients += (url -> Set.empty[ActorRef])
      }

      clients(url) += sender

    case Result(url, links) =>
      context.stop(linkcontroller(url))

      clients(url) foreach (_ ! CrawledUrls(url, links))
      clients -= url

      linkcontroller -= url
  }
}
