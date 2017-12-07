/*
package web.crawler.net
import akka.actor._
import akka.actor.{Actor, ActorRef, Props}
import web.crawler.net.ClientActor.{StartCrawling, CrawledUrls}
import web.crawler.net.LinkChecker.Result
import scala.collection.mutable
object ClientActor {
  case class StartCrawling(url: String, depth: Int) {}
  case class CrawledUrls(url: String, links: Set[String]) {}
}
class ClientActor extends Actor {

  val clients: mutable.Map[String, Set[ActorRef]] =
    collection.mutable.Map[String, Set[ActorRef]]()
  val controllers: mutable.Map[String, ActorRef] =
    mutable.Map[String, ActorRef]()

  def receive = {

    case StartCrawling(url, depth) =>
      val controller = controllers get url
      if (controller.isEmpty) {
        controllers += (url -> context.actorOf(
          Props[LinkChecker](new LinkChecker(url, depth))))
        clients += (url -> Set.empty[ActorRef])
      }
      clients(url) += sender

    case Result(url, links) =>
      context.stop(controllers(url))

      clients(url) foreach (_ ! CrawledUrls(url, links))
      clients -= url

      controllers -= url
  }
}





/*
  val _system: ActorSystem = ActorSystem.create("hello-system")

  val serveractor: ActorRef = _system.actorOf(ServerActor.props)
  val url: String = "https://www.google.co.in/"

  serveractor ! url

  Thread.sleep(2000)

  _system.terminate
}*/
*/
