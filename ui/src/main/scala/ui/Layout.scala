package ui

import Routes.{LoginPage, Page, SignupPage}
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import vdom.html_<^._
object Layout {

  val navMenu = ScalaComponent
    .builder[RouterCtl[Page]]("Menu")
    .render_P { ctl =>
      <.nav(
        ^.cls := "navbar navbar-expand-sm navbar-light bg-light",
        <.a(^.cls := "navbar-brand", "DummyApp"),
        <.div(
          ^.cls := "collapse navbar-collapse",
          <.div(
            ^.cls := "navbar-nav",
            <.a(^.cls := "nav-item nav-link", ctl setOnClick LoginPage, "Login"),
            <.a(^.cls := "nav-item nav-link", ctl setOnClick SignupPage, "Signup")
          )
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  def render(c: RouterCtl[Page], r: Resolution[Page]) =
    <.div(
      navMenu(c),
      <.div(^.cls := "container", r.render())
    )

}