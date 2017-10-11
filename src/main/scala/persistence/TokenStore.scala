package persistence

import java.util.UUID

import cats.data.OptionT
import cats.effect.Effect
import fs2.async.Ref
import models.{AuthInfo, User}
import tsec.authentication._
import cats.syntax.all._
import tsec.cipher.symmetric.imports.AES128

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext

sealed abstract class TokenStore[F[_]: Effect]
    extends BackingStore[F, UUID, AuthEncryptedCookie[AES128, UUID]] {
  protected val ref: Ref[F, HashMap[UUID, AuthEncryptedCookie[AES128, UUID]]]

  def put(elem: AuthEncryptedCookie[AES128, UUID]): F[Int] = {
    ref
      .modify(_ + (elem.id -> elem))
      .map(modified => modified.now.size - modified.previous.size)
  }

  def get(id: UUID): OptionT[F, AuthEncryptedCookie[AES128, UUID]] =
    OptionT(ref.get.map(_.get(id)))

  def update(v: AuthEncryptedCookie[AES128, UUID]): F[Int] =
    ref
      .modify(_.updated(v.id, v))
      .map(_ => 1)

  def delete(id: UUID): F[Int] =
    ref
      .modify(_ - id)
      .map(modified => modified.previous.size - modified.now.size)

}

object TokenStore {
  def apply[F[_]: Effect](implicit ec: ExecutionContext): F[TokenStore[F]] =
    Ref
      .initialized(HashMap.empty[UUID, AuthEncryptedCookie[AES128, UUID]])
      .map { m =>
        new TokenStore[F] {
          protected val ref
            : Ref[F, HashMap[UUID, AuthEncryptedCookie[AES128, UUID]]] = m
        }
      }
}
