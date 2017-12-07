package web.crawler.net

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import web.crawler.net.HttpRequest.Done
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
  context.setReceiveTimeout(10 seconds)

  def receive = {
    case CheckUrl(url, depth) =>
      if (!visitedurls(url) && depth >= 0)
        children += context.actorOf(
          Props[HttpRequest](new HttpRequest(url, depth - 1)))
      visitedurls += url

    case Done =>
      children -= sender
      if (children.isEmpty) context.parent ! Result(root, visitedurls)

    case ReceiveTimeout => children foreach (_ ! HttpRequest.Abort)
  }
}

/*
object LinkChecker {
  case class Done() {}
  case class Abort() {}
  case class CheckUrl(url: String, depth: Int) {}
  case class Result(url: String, links:Set[String]) {}
}
class LinkChecker(url: String, depth: Int) extends Actor {
//  import LinkChecker._
  /*import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClient, AsyncHttpClientConfig, Response}

  import scala.concurrent.{Future, Promise}

  val config = new AsyncHttpClientConfig.Builder()
  val client = new AsyncHttpClient(
    config
      .setFollowRedirect(true)
      .setExecutorService(Executors.newWorkStealingPool(64))
      .build())
  def get(url: String): Future[String] = {
    val promise = Promise[String]()
    val request = client.prepareGet(url).build()
    client.executeRequest(
      request,
      new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          promise.success(response.getResponseBody)
          response
        }
        override def onThrowable(t: Throwable): Unit = {
          promise.failure(t)
        }
      }
    )
    promise.future
  }*/

  var visitedLinks = Set.empty[String]
  var links = Set.empty[ActorRef]
  self ! CheckUrl(url, depth)
  context.setReceiveTimeout(10 seconds)

  def getAllLinks(content: String): Iterator[String] = {
    Jsoup
      .parse(content, this.url)
      .select("a[href]")
      .iterator()
      .asScala
      .map(_.absUrl("href"))
  }



  def receive = {

    case CheckUrl(url, depth) =>
      if (!visitedLinks(url) && depth > 0)
        links += context.actorOf(
          Props[HttpRequest](new HttpRequest(url, depth - 1)))

      visitedLinks += url
    case _: Status.Failure => stop()

//    case Abort => stop()
    case Abort => stop()
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }
}*/
