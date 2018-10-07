package com.eztier.postgres.async

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import io.getquill.{PostgresAsyncContext, SnakeCase}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe._
import com.eztier.postgres.eventstore.models._

case class MtStreams
(
  typeName: String
)

object CommandRunner {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  lazy val ctx = new PostgresAsyncContext(SnakeCase, "development.quill.postgres.ctx")

  import ctx._

  def searchQuery[A](search: String) = quote {
    () => infix"""SELECT name FROM hl7.mt_streams WHERE name *~ '$search'""".as[Query[A]]
  }

  implicit class ILike(s1: String) {
    def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
  }

  def read2(search: String): Future[Seq[MtStreams]] =
    ctx.run(querySchema[MtStreams]("hl7.mt_streams", _.typeName -> "type").filter(_.typeName ilike lift("%" + search + "%")))
    
  def search[A <: Model](term: String, schema: String = "hl7")(implicit searchable: Searchable[A], typeTag: TypeTag[A]): Future[Seq[A]] = {
    val t = schema + "." + typeTag.tpe.typeSymbol.name.toString.toLowerCase

    searchable.search(ctx, term)
  }
}
