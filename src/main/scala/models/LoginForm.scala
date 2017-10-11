package models

import cats.effect.Effect
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.EntityDecoder

case class LoginForm(username: String, password: String)

object LoginForm {
  object LoginError extends Exception {
    override def getMessage: String = "Login Error"

    override def fillInStackTrace(): Throwable = this
  }

  implicit def decoder[F[_]: Effect]: EntityDecoder[F, LoginForm] = jsonOf[F, LoginForm]

}
