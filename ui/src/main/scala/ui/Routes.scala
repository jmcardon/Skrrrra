package ui

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.Implicits._

object Routes {

  sealed trait Page
  case object LoginPage  extends Page
  case object SignupPage extends Page

  val router = RouterConfigDsl[Page].buildConfig { dsl =>
    import dsl._
    (emptyRule
      | staticRoute(root, LoginPage) ~> render(LoginDisplay.component())
      | staticRoute("#signup", SignupPage) ~> render(SignupDisplay.component()))
      .notFound(redirectToPage(LoginPage)(Redirect.Replace))
      .renderWith(Layout.render)
  }

}
