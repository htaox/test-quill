package com.eztier.test

import org.scalatest.{BeforeAndAfter, Failed, FunSpec, Matchers}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.eztier.postgres.async._

import scala.concurrent.Await
import scala.concurrent.duration._

class TestQuillSpec extends FunSpec with Matchers {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()


  val runner = CommandRunner()

  val f = runner.read("a")

  val r = Await.result(f, 500 millis)

  r.foreach(println(_))

  println("Done")

}
