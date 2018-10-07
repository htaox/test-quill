package com.eztier.rest.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.common
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.util.ByteString
import com.eztier.postgres.eventstore.models.Patient
import com.eztier.postgres.async.CommandRunner
import com.eztier.rest.responses.SearchPatientJsonProtocol._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait SearchStreamRoutes {
  implicit val actorSystem: ActorSystem
  implicit val streamMaterializer: ActorMaterializer
  implicit val executionContext: ExecutionContext
  lazy val httpStreamingRoutes = streamingJsonRoute
  lazy val httpInfoStreamingRoutes = streamingInfoRoute
  lazy val httpStreamingSearchRoutes = streamingSearchRoute

  implicit val jsonStreamingSupport: akka.http.scaladsl.common.JsonEntityStreamingSupport = EntityStreamingSupport.json()
  
  def streamingInfoRoute =
    path("info") {
      get {
        val sourceOfNumbers = Source(1 to 15)
        val byteStringSource =
          sourceOfNumbers.map(num => s"mrn:$num")
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)
            .map(_.toString)
            .map(s => ByteString(s))

        complete(HttpEntity(`text/plain(UTF-8)`, byteStringSource))
      }
    }

  /*
    @test
      for i in {1..10000}; do curl localhost:9000/streaming-json & done
  */
  def streamingJsonRoute =
    path("streaming-json") {
      get {
        val sourceOfNumbers = Source(1 to 15)
        val sourceOfSearchMessages =
          sourceOfNumbers.map(num => Patient(s"name:$num"))
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)

        complete(sourceOfSearchMessages)
      }
    }
    
  def searchEventstore(term: String) = Flow[Patient].map {
    s =>
      CommandRunner.search(term)
  }
    
  /*
    @test
      curl -XPOST -H 'Content-Type:application/json'  -d '{"name": "abc"}' localhost:9000/search
  */
  def streamingSearchRoute =
    path("search") {
      post {
        entity(as[Patient]) { p =>

          val resp = Source.single(p)
            .via(searchEventstore(p.name))
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)

          // complete(HttpEntity(`text/plain(UTF-8)`, resp))
          complete(resp)
        }
      }

    }

}
