package tsec

import java.util.concurrent.Executors

import cats.effect.IO
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import persistence.{PasswordStore, TokenStore, UserStore}
import services._

import scala.concurrent.duration._
import tsec.authentication._
import tsec.cipher.symmetric.imports.{AES128, SecretKey}

import scala.concurrent.ExecutionContext

object BlazeExample extends StreamApp[IO] with Http4sDsl[IO] {

  val authenticatorSettings = TSecCookieSettings("tsec-auth", secure = false, httpOnly = true)

  val route = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }

  def wire(
      userStore: UserStore[IO],
      tokenStore: TokenStore[IO],
      passwordStore: PasswordStore[IO],
      key: SecretKey[AES128]
  ): IO[BlazeBuilder[IO]] = {
    val authenticator = EncryptedCookieAuthenticator.withBackingStore(
      authenticatorSettings,
      tokenStore,
      userStore,
      key,
      15.minutes,
      None
    )
    val userAuthService = UserAuthenticationService[IO](userStore, passwordStore, authenticator)
    val requestAuth     = RequestAuthenticator.encryptedCookie(authenticator)
    val authedService   = AuthenticatedService[IO](requestAuth)
    IO.pure {
      BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .mountService(route, "/")
        .mountService(userAuthService.signupRoute)
        .mountService(userAuthService.loginRoute)
        .mountService(authedService.helloFromAuthentication)
    }
  }

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, Nothing] = {
    implicit val refEc = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

    val wiring = for {
      userStore     <- UserStore[IO]
      tokenStore    <- TokenStore[IO]
      passwordStore <- PasswordStore[IO]
      symmetricKey  <- AES128.generateLift[IO]
      wired         <- wire(userStore, tokenStore, passwordStore, symmetricKey)
    } yield wired

    Stream.eval(wiring).flatMap(_.serve)
  }

}
