package ui

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.{document, window}
import org.scalajs.dom.html.Input
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global

object LoginDisplay {

  case class Login(username: String, password: String)

  object Login {
    def initial = Login("", "")
  }

  class Backend(bs: BackendScope[Unit, Login]) {

    def fetch = Callback {
      val form = Login(
        document.getElementById("username").asInstanceOf[Input].value,
        window.btoa(document.getElementById("password").asInstanceOf[Input].value)
      )

      Ajax
        .post(
          "http://localhost:8081/api/login",
          data = form.asJson.toString(),
          headers = Map("Content-Type" -> "application/json")
        )
        .map(_.responseText)
    }

    def render: VdomElement = {
      val state = bs.state.runNow()

      <.div(
        ^.cls := "container",
        <.div(
          <.form(
            ^.method := "post",
            ^.onSubmit --> fetch.flatMap(_ =>  CallbackTo.pure(false)),
            ^.cls := "container",
            <.div(
              ^.cls := "form-group",
              <.label(
                ^.cls := "col-sm-6",
                ^.`for` := "username",
                "Username"
              ),
              <.input(
                ^.id := "username",
                ^.cls := "form-control",
                ^.`type` := "text"
              )
            ),
            <.div(
              ^.cls := "form-group",
              <.label(
                ^.cls := "col-sm-6",
                ^.`for` := "password",
                "Password"
              ),
              <.input(
                ^.id := "password",
                ^.cls := "form-control",
                ^.`type` := "password"
              )
            ),
            <.button(
              ^.cls := "btn btn-primary",
              "submit",
              ^.onClick --> fetch
            )
          )
        )
      )
    }
  }

  lazy val component = ScalaComponent
    .builder[Unit]("TaskOverview")
    .initialState(Login.initial)
    .renderBackend[Backend]
    .build

}
