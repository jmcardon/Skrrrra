package models

import java.util.UUID

import tsec.passwordhashers.imports.SCrypt

case class User(id: UUID, username: String, age: Int)

case class AuthInfo(id: UUID, parentId: UUID, password: SCrypt)