package services

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl
import tsec.authentication._

class AuthenticatedService[F[_]: Effect]
(Authed: Authenticator[F]) extends Http4sDsl[F] {

  val helloFromAuthentication = Authed {
    case GET -> Root / "home" asAuthed user =>
      Ok(s"Hi ${user.username}!")
  }

}
