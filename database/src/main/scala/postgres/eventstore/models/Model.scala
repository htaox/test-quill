package com.eztier.postgres.eventstore.models

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.getquill.{PostgresAsyncContext, SnakeCase}

import scala.concurrent.Future

trait Model {
  def name: String
}

trait Searchable[A <: Model] {
  def search(ctx: PostgresAsyncContext[SnakeCase.type], term: String, schema: String = "hl7"): Future[Seq[A]]
}

object Searchable {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit object PatientSearch extends Searchable[Patient] {
    override def search(ctx: PostgresAsyncContext[SnakeCase.type], term: String, schema: String = "hl7"): Future[Seq[Patient]] = {
      import ctx._

      implicit class ILike(s1: String) {
        def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
      }

      val entity = s"$schema.patient"
      ctx.run(querySchema[Patient]("hl7.patient").filter(_.name ilike lift("%" + term + "%")))
    }
  }

}
