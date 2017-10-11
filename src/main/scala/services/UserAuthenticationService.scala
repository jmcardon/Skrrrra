package services

import java.util.UUID
import cats.data.OptionT
import cats.effect.Effect
import org.http4s.{HttpService, Response, Status}
import org.http4s.dsl.Http4sDsl
import models._
import tsec.passwordhashers._
import tsec.passwordhashers.imports._
import cats.syntax.all._
import tsec.common._
import persistence.{PasswordStore, UserStore}
import tsec.authentication.EncryptedCookieAuthenticator
import tsec.cipher.symmetric.imports.AES128
import models.LoginForm.LoginError
import models.SignupForm.SignupError

class UserAuthenticationService[F[_]](
    userStore: UserStore[F],
    authStore: PasswordStore[F],
    authenticator: EncryptedCookieAuthenticator[F, AES128, UUID, User]
)(implicit F: Effect[F])
    extends Http4sDsl[F] {

  private def checkOrRaise(rawFromLogin: String, hashed: SCrypt): F[Unit] =
    if (rawFromLogin.base64Bytes.toAsciiString.checkWithHash(hashed))
      F.unit
    else
      F.raiseError[Unit](LoginError)

  val signupRoute: HttpService[F] = HttpService[F] {
    case request @ POST -> Root / "api" / "signup" =>
      val response = for {
        signup   <- request.as[SignupForm]
        exists   <- userStore.exists(signup.username).getOrRaise(SignupError)
        password <- F.pure(signup.password.base64Bytes.toAsciiString.hashPassword[SCrypt])
        newUser = User(UUID.randomUUID(), signup.username, signup.age)
        _      <- userStore.put(newUser)
        _      <- authStore.put(AuthInfo(UUID.randomUUID(), newUser.id, password))
        cookie <- authenticator.create(newUser.id).getOrRaise(LoginError)
        o      <- Ok("Successfully signed up!")
      } yield authenticator.embed(o, cookie)

      response
        .handleError(_ => Response(Status.BadRequest))
  }

  val loginRoute: HttpService[F] = HttpService[F] {
    case request @ POST -> Root / "api" / "signup" =>
      for {
        login    <- request.as[LoginForm]
        user     <- userStore.exists(login.username).getOrRaise(LoginError)
        authInfo <- authStore.get(user.id).getOrRaise(LoginError)
        _        <- checkOrRaise(login.password, authInfo.password)
        cookie   <- authenticator.create(user.id).getOrRaise(LoginError)
        o        <- Ok()
      } yield authenticator.embed(o, cookie)
  }

}
