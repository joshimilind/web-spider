package web.crawler.net

import akka.actor._
import web.crawler.net.GetterActor.Done
import web.crawler.net.LinkChecker.{CheckUrl, Result}

import scala.concurrent.duration._

object LinkChecker {
  case class CheckUrl(url: String, depth: Int) {}
  case class Result(url: String, links: Set[String]) {}
}

class LinkChecker(root: String, originalDepth: Int) extends Actor {

  var visitedurls = Set.empty[String]
  var children = Set.empty[ActorRef]

  self ! CheckUrl(root, originalDepth)
  context.setReceiveTimeout(20 seconds)

  def receive = {

    case CheckUrl(url, depth) =>
      /**
        * checking if url to be crawled is not already visited
        */
      if (!visitedurls(url) && depth >= 0)
        children += context.actorOf(
          Props[GetterActor](new GetterActor(url, depth - 1)))

      visitedurls += url

    case Done =>
      children -= sender
      if (children.isEmpty) context.parent ! Result(root, visitedurls)

    case ReceiveTimeout => children foreach (_ ! GetterActor.Abort)
  }
}
