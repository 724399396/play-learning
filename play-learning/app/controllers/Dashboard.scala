package controllers

import play.api.mvc._
import concurrent.{ExecutionContext, Future}

object Dashboard extends Controller {
  def backlog(warehouse: String) = Action {
    import ExecutionContext.Implicits.global

    val backlogFuture = scala.concurrent.Future {
      models.Order.backlog(warehouse)
    }

    Ok("")
  }
}
