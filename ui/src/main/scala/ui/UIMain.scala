package ui
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import org.scalajs.dom.document

object UIMain {
  def main(args: Array[String]): Unit = {

    val router = Router(BaseUrl.fromWindowOrigin_/, Routes.router.logToConsole)
    router().renderIntoDOM(document.getElementById("app"))
  }
}
