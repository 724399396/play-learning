package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.iteratee.Iteratee
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TwitterStreamFetch @Inject()(val ws: WSClient, val app: Application) {
  val consumerKey = ConsumerKey("52xEY4sGbpLO1FCQRaiAg",
    "KpnmEeDM6XDwS59FDcAmVMQbui8mcceNASj7xFJc5WY")

  val accessToken = RequestToken(
    "16905598-cIPuAsWUI47Fk78guCRTa7QX49G0nOQdwv2SA6Rjz",
    "yEKoKqqOjo4gtSQ6FSsQ9tbxQqQZNq7LB5NGsbyKU")

  val loggingIteratee = Iteratee.foreach[Array[Byte]] { chunk =>
    val chunkString = new String(chunk, "UTF-8")
    println(chunkString)
  }

  def start = {
    ws.url("https://stream.twitter.com/1/statuses/sample.json")
      .sign(OAuthCalculator(consumerKey, accessToken))
      .get(_ => loggingIteratee)
  }
}
