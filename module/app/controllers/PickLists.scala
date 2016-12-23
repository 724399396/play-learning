package controllers

import java.util.Date
import models.PickList
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import javax.inject.Inject
import play.api.Configuration

class PickLists @Inject()(val messagesApi: MessagesApi, val config: Configuration) extends Controller with I18nSupport {
  def preview(warehouse: String) = Action { implicit request =>
    val pickList = PickList.find(warehouse)
    val timestamp = new java.util.Date
    Ok("")
  }

  def sendAsync(warehouse: String) = Action { implicit request =>
    import ExecutionContext.Implicits.global
    Future {
      val pickList = PickList.find(warehouse)
    }
    Redirect(routes.PickLists.index())
  }

  private def send(html: play.twirl.api.Html) = {

  }

  def index = Action {
    Ok("")
  }
}
