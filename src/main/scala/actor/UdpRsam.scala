package actor

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import com.sksamuel.elastic4s.{ElasticClient, ElasticDsl, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient
import dto.Rsam


class UdpRsam(remote: InetSocketAddress) extends Actor with ElasticDsl {
  import context.system
  IO(Udp) ! Udp.Bind(self, remote)

  lazy val client = ElasticClient(JavaClient(ElasticProperties("http://localhost:9200")))

  val indexName = "rsam"

  def buildIndices(): Unit = {
    client.execute {
      createIndex(indexName).mapping(
        properties(
          textField("station"),
          textField("channel"),
          doubleField("mean"),
          doubleField("median"),
          doubleField("min"),
          doubleField("max"),
          dateField("time")
        )
      )
    }.await
  }

  def receive = {
    case Udp.Bound(local) =>
      println(s"Building RSAM indices...")
      buildIndices()
      println(s"Listening for RSAM on $remote")
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val rsam = parseRsamLite(data.utf8String)
      println(rsam)
      indexRsam(rsam)


    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }

  def indexRsam(rsam: Rsam): Unit = {
    client.execute(
      indexInto(indexName).fields(
        "station" -> rsam.station,
        "channel" -> rsam.channel,
        "mean" -> rsam.mean,
        "median" -> rsam.median,
        "min" -> rsam.min,
        "max" -> rsam.max,
        "time" -> System.currentTimeMillis()
      )
    )
  }

  def parseRsamLite(msg: String): Rsam = {
    val fields = msg
      .split("\\|")
      .map(_.split(":"))
      .map{ case arr => (arr(0), arr(1))}
      .toMap

    Rsam(
      fields("stn"),
      fields("ch"),
      fields("mean").toDouble / 1000.00,
      fields("med").toDouble / 1000.00,
      fields("min").toDouble / 1000.00,
      fields("max").toDouble / 1000.00
    )
  }
}