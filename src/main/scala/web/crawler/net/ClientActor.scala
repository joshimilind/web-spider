package web.crawler.net

import akka.actor.{ActorRef, ActorSystem}
object ClientActor extends App {

  val _system: ActorSystem = ActorSystem.create("hello-system")

  val serveractor: ActorRef = _system.actorOf(ServerActor.props)
  val url: String = "https://www.google.co.in/"

  serveractor ! url

  Thread.sleep(2000)

  _system.terminate
}
