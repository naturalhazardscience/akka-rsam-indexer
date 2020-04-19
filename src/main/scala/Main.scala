import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import actor.{UdpRsam, UdpSupervisor}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("AkkaUdpSystem")

    val supervisor = system.actorOf(Props[UdpSupervisor])

    supervisor ! Props(new UdpRsam(new InetSocketAddress("172.16.0.1", 4444)))
  }
}
