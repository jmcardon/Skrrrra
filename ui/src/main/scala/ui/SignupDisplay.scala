package ui

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.{document, window}
import org.scalajs.dom.html.Input
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global

object SignupDisplay {

  case class SignupForm(username: String, age: Int, password: String)

  object SignupForm {
    def initial = SignupForm("", 0, "")
  }

  class Backend(bs: BackendScope[Unit, SignupForm]) {

    def fetch = Callback {
      val form = SignupForm(
        document.getElementById("username").asInstanceOf[Input].value,
        document.getElementById("age").asInstanceOf[Input].value.toInt,
        window.btoa(document.getElementById("password").asInstanceOf[Input].value)
      )

      Ajax
        .post(
          "http://localhost:8081/api/signup",
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
              ^.cls := "form-control col-sm-6",
              ^.`type` := "text"
            )
          ),
          <.div(
            ^.cls := "form-group",
            <.label(
              ^.cls := "col-sm-6",
              ^.`for` := "age",
              "Age"
            ),
            <.input(
              ^.id := "age",
              ^.cls := "form-control col-sm-6",
              ^.`type` := "number"
            )
          ),
          <.div(
            ^.cls := "form-group row",
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
    }
  }

  lazy val component = ScalaComponent
    .builder[Unit]("Signup")
    .initialState(SignupForm.initial)
    .renderBackend[Backend]
    .build

}
