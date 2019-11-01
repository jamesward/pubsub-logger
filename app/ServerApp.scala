import play.api.http.Writeable
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsJson, AnyContentAsRaw, Codec, Results}
import play.api.routing.Router
import play.api.routing.sird._
import play.api.{BuiltInComponents, Logger, Mode}
import play.core.server.{NettyServerComponents, ServerConfig}

object ServerApp extends App {

  val components = new NettyServerComponents with BuiltInComponents {

    val port = sys.env.getOrElse("PORT", "8080").toInt
    val mode = if (configuration.get[String]("play.http.secret.key").contains("changeme")) Mode.Dev else Mode.Prod

    override lazy val serverConfig = ServerConfig(port = Some(port), mode = mode)

    val action = Action { request =>
      Logger(this.getClass).logger.info(request.method)
      Logger(this.getClass).logger.info(request.path)
      Logger(this.getClass).logger.info(request.headers.toString)
      val logBody = request.body match {
        case r: AnyContentAsRaw => r.raw.asBytes().map(_.utf8String).toString
        case _ => request.body.toString
      }
      Logger(this.getClass).logger.info(logBody)

      // todo: this could be better
      request.body match {
        case c: AnyContentAsJson => Results.Ok(c.json)
        case AnyContentAsEmpty => Results.Ok(EmptyContent())
        case _ => Results.Ok(logBody)
      }

    }

    lazy val router = Router.from {
      case GET(_) | POST(_) | PUT(_) | DELETE(_) | PATCH(_) | OPTIONS(_) | HEAD(_) => action
    }

    override def httpFilters = Seq.empty
  }

  val server = components.server

  while (!Thread.currentThread.isInterrupted) {}

  server.stop()

}
