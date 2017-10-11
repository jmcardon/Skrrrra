package persistence

import java.util.UUID

import cats.data.OptionT
import cats.effect.Effect
import fs2.async.Ref
import models.{AuthInfo, User}
import tsec.authentication._
import cats.syntax.all._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext

sealed abstract class PasswordStore[F[_]: Effect]
    extends BackingStore[F, UUID, AuthInfo] {
  protected val ref: Ref[F, HashMap[UUID, AuthInfo]]

  def put(elem: AuthInfo): F[Int] = {
    ref
      .modify(_ + (elem.parentId -> elem))
      .map(modified => modified.now.size - modified.previous.size)
  }

  def get(id: UUID): OptionT[F, AuthInfo] =
    OptionT(ref.get.map(_.get(id)))

  def update(v: AuthInfo): F[Int] =
    ref
      .modify(_.updated(v.parentId, v))
      .map(_ => 1)

  def delete(id: UUID): F[Int] =
    ref
      .modify(_ - id)
      .map(modified => modified.previous.size - modified.now.size)

  def getPass(userId: UUID): OptionT[F, AuthInfo] =
    OptionT(ref.get.map(_.get(userId)))
}

object PasswordStore {
  def apply[F[_]: Effect](implicit ec: ExecutionContext): F[PasswordStore[F]] =
    Ref.initialized(HashMap.empty[UUID, AuthInfo]).map {
      m => new PasswordStore[F] {
        protected val ref: Ref[F, HashMap[UUID, AuthInfo]] = m
      }
    }
}