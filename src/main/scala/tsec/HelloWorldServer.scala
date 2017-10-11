package tsec

import cats.effect.IO
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object BlazeExample extends StreamApp[IO] with Http4sDsl[IO] {

  val route = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(route,"/")
      .serve
  }
}
