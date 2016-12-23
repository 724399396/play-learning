package com.github.playforscala.barcodes

import play.api.mvc.{Action, Controller}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class BarcodesController extends Controller {
  def barcode(ean: Long) = Action.async {
      Barcodes.renderImage(ean) map {
        case Success(image) => Ok(image).as(Barcodes.mimeType)
        case Failure(e) =>
          BadRequest("Couldn't generate bar code. Error: " + e.getMessage)
      }
  }
}
