package controllers

import play.api.mvc.{Action, Controller, Flash}
import models.Product
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.i18n._
import play.api.Configuration
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText }

class Products @Inject() extends Controller {
  def list = Action { implicit request =>
    val products = Product.findAll
    Ok(views.html.products.list(products))
  }

  def show(ean: Long) = Action { implicit request =>
    Product.findByEan(ean).map { product =>
      Ok(views.html.products.details(product))
    }.getOrElse(NotFound)
  }

  def save = Action { implicit request =>
    val newProductForm = productForm.bindFromRequest()

    newProductForm.fold(
      hasErrors = { form =>
        Redirect(routes.Products.newProduct())
          .flashing(Flash(form.data) +
            ("error" -> Messages.get("validation.errors")))
      },

      success = { newProduct =>
        Product.add(newProduct)
        val message = Messages.get("products.new.success", newProduct.name)
        Redirect(routes.Products.show(newProduct.ean))
          .flashing("success" -> message)
      }
    )
  }

  def newProduct = Action { implicit request =>
    val form = if (request.flash.get("error").isDefined)
      productForm.bind(request.flash.data)
    else
      productForm
    Ok(views.html.products.editProduct(form))
  }

  private val productForm: Form[Product] = Form (
    mapping (
      "ean" -> longNumber.verifying(
        "validation.ean.duplicate", Product.findByEan(_).isEmpty),
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    ) (Product.apply) (Product.unapply)
  )
}
