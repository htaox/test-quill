package com.eztier.postgres.async

import io.getquill.{PostgresAsyncContext, SnakeCase}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class MtStreams
(
  typeName: String
)

case class CommandRunner() {
  lazy val ctx = new PostgresAsyncContext(SnakeCase, "development.quill.postgres.ctx")

  import ctx._

  val serchQuery = quote {
    (search: String) => infix"""SELECT type FROM hl7.mt_streams WHERE name *~ '$search'""".as[Query[(String, String)]]
  }

  implicit class ILike(s1: String) {
    def ilike(s2: String) = quote(infix"$s1 ilike $s2".as[Boolean])
  }

  def read(search: String): Future[Seq[MtStreams]] =
    ctx.run(querySchema[MtStreams]("hl7.mt_streams", _.typeName -> "type").filter(_.typeName ilike lift("%" + search + "%")))
}
