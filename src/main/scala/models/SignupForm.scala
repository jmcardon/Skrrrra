package models

import cats.effect.Effect
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.EntityDecoder

case class SignupForm(username: String, age: Int, password: String) //All in base64

object SignupForm {
  final object SignupError extends Exception {
    override def getCause: Throwable = this

    override def getMessage: String = "Signup Error"

    override def fillInStackTrace(): Throwable = this
  }

  implicit def entityD[F[_]: Effect]: EntityDecoder[F, SignupForm] = jsonOf[F, SignupForm]
}