package persistence

import java.util.UUID

import cats.data.OptionT
import cats.effect.Effect
import fs2.async.Ref
import models.User
import tsec.authentication._
import cats.syntax.all._

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext

sealed abstract class UserStore[F[_]: Effect]
    extends BackingStore[F, UUID, User] {
  protected val ref: Ref[F, HashMap[UUID, User]]

  def put(elem: User): F[Int] = {
    ref
      .modify(_ + (elem.id -> elem))
      .map(modified => modified.now.size - modified.previous.size)
  }

  def get(id: UUID): OptionT[F, User] =
    OptionT(ref.get.map(_.get(id)))

  def update(v: User): F[Int] =
    ref
      .modify(_.updated(v.id, v))
      .map(_ => 1)

  def delete(id: UUID): F[Int] =
    ref
      .modify(_ - id)
      .map(modified => modified.previous.size - modified.now.size)

  def exists(username: String): OptionT[F, User] = {
    OptionT(ref.get.map(_.values.find(_.username == username)))
  }
}

object UserStore {
  def apply[F[_]: Effect](implicit ec: ExecutionContext): F[UserStore[F]] =
    Ref.initialized(HashMap.empty[UUID, User]).map {
      m => new UserStore[F] {
        protected val ref: Ref[F, HashMap[UUID, User]] = m
      }
    }
}