package tsec

import java.util.concurrent.Executors

import cats.effect.IO
import fs2.Stream
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.http4s.util.StreamApp
import persistence.{PasswordStore, TokenStore, UserStore}
import services._
import tsec.authentication._
import tsec.cipher.symmetric.imports.{AES128, SecretKey}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends StreamApp[IO] with Http4sDsl[IO] {

  val corsConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = true,
    maxAge = 100000
  )

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
        .bindHttp(8081, "localhost")
        .mountService(CORS(route, corsConfig),"/")
        .mountService(CORS(userAuthService.signupRoute))
        .mountService(CORS(userAuthService.loginRoute))
        .mountService(CORS(authedService.helloFromAuthentication))
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
