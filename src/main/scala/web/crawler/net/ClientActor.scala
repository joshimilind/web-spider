package web.crawler.net

import akka.actor.{ActorRef, ActorSystem}

object ClientActor extends App {

  val _system: ActorSystem = ActorSystem.create("hello-system")
  val actor: ActorRef = _system.actorOf(ServerActor.props)
  val url: String =
    ("https://alvinalexander.com/source-code/scala/scala-open-url-inputstream-method-httpurlconnection")

  actor ! url

  Thread.sleep(2000)

  _system.shutdown();
  _system.awaitTermination();
}