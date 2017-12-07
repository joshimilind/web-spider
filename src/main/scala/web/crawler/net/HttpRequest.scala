package web.crawler.net

import java.net.URL
import java.util.concurrent.Executors

import akka.actor.{Actor, Status}
import com.ning.http.client.{
  AsyncCompletionHandler,
  AsyncHttpClient,
  AsyncHttpClientConfig
}
import com.ning.http.client.Response
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object HttpRequest {
  case class Done() {}
  case class Abort() {}
}

class HttpRequest(url: String, depth: Int) extends Actor {
  import HttpRequest._

  implicit val ec = context.dispatcher

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
  }

  val currentHost = new URL(url).getHost
  get(url) onComplete {
    case Success(body) => self ! body
    case Failure(err)  => self ! Status.Failure(err)
  }

  def getAllLinks(content: String): Iterator[String] = {
    Jsoup
      .parse(content, this.url)
      .select("a[href]")
      .iterator()
      .asScala
      .map(_.absUrl("href"))
  }
  def receive = {

    case body: String =>
      getAllLinks(body)
        .filter(link => link != null && link.length > 0)
        .filter(link => currentHost == new URL(link).getHost)
        .foreach(context.parent ! LinkChecker.CheckUrl(_, depth))

      stop

    case _: Status.Failure => stop()

    case Abort => stop()
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }
}
