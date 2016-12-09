package controllers

import play.api.mvc.{Action, Controller}
import models.Product
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.Configuration
import javax.inject.Inject

class Products @Inject() extends Controller {
  println("here")
  def list = Action { implicit request =>
    val products = Product.findAll
    Ok(views.html.products.list(products))
  }
}
