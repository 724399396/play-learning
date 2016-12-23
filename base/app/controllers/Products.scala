package controllers

import play.api.mvc.{Action, Controller, Flash}
import models.Product
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import javax.inject.Inject

import org.joda.time.LocalDate
import play.api.Configuration
import play.api.data.{Form, FormError}
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.data.format.Formatter

import scala.util.Try

class Products @Inject()(val messagesApi: MessagesApi, val config: Configuration) extends Controller with I18nSupport {
  def list = Action { implicit request =>
    val products = Product.getAll
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
            ("error" -> Messages("validation.errors")))
      },

      success = { newProduct =>
        Product.add(newProduct)
        val message = Messages("products.new.success", newProduct.name)
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
    ) ( (x,y,z) => Product.apply(1,x,y,z)) (x => Product.unapply(x).map{case (_,x,y,z) => (x,y,z)})
  )

  implicit val localDateFormatter = new Formatter[LocalDate] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
      data.get(key) map { value =>
        Try {
         Right(LocalDate.parse(value))
        } getOrElse Left(Seq(FormError(key, "error.date", Nil)))
      } getOrElse Left(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: LocalDate): Map[String, String] =
      Map(key -> value.toString)

    override val format = Some(("date.format", Nil))
  }
}
