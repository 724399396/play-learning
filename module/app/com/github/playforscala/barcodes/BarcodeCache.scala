package com.github.playforscala.barcodes

import akka.actor.Actor

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class BarcodeCache extends Actor {
  var imageCache = Map[Long, Future[Array[Byte]]]()

  def receive = {
    case RenderImage(ean) => {
      val futureImage = imageCache.get(ean) match {
        case Some(futureImage) => futureImage
        case None => {
          val futureImage = Future { ean13BarCode(ean, "image/png")}
          imageCache += (ean -> futureImage)
          futureImage
        }
      }

      val client = sender()

      futureImage.onComplete {
        client ! RenderResult(_)
      }
    }
  }

  def ean13BarCode(ean: Long, mimeType: String): Array[Byte] = {
    import java.awt.image.BufferedImage
    import java.io.ByteArrayOutputStream

    import org.krysalis.barcode4j.impl.upcean.EAN13Bean
    import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider

    val ImageResolution = 144
    val output: ByteArrayOutputStream = new ByteArrayOutputStream
    val canvas: BitmapCanvasProvider =
      new BitmapCanvasProvider(output, mimeType, ImageResolution,
        BufferedImage.TYPE_BYTE_BINARY, false, 0)

    val barcode = new EAN13Bean()
    barcode.generateBarcode(canvas, String valueOf ean)
    canvas.finish

    output.toByteArray
  }
}

case class RenderImage(ean: Long)
case class RenderResult(image: Try[Array[Byte]])
