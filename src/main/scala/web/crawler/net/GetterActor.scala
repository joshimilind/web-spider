package web.crawler.net

import akka.actor._
import org.jsoup.Jsoup
import web.crawler.net.GetterActor.{Abort,Done}
import java.net.URL
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

object GetterActor {
  case class Done() {}
  case class Abort() {}
}
class GetterActor(url:String, depth:Int) extends Actor  {

  /**
    * Calling method get() from singleton for establishing connection
    */
  val currentHost = new URL(url).getHost
  HttpRequest.get(url) onComplete {
    case Success(body) => self ! body
    case Failure(err)  => self ! Status.Failure(err)
  }
  /**
    * Once the connection is established getting all links by leaving
    * Message as body in the Mailbox of GetterActor
    */

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
