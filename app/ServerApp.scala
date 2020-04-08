import play.api.mvc.Results.EmptyContent
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, AnyContentAsRaw, AnyContentAsText, Results}
import play.api.routing.Router
import play.api.routing.sird._
import play.api.{Logger, Mode}
import play.core.server.{DefaultAkkaHttpServerComponents, ServerConfig}

import scala.util.Try

object ServerApp extends App {

  val components = new DefaultAkkaHttpServerComponents {
    private[this] lazy val port = sys.env.get("PORT").flatMap(s => Try(s.toInt).toOption).getOrElse(9000)
    private[this] lazy val mode = if (configuration.get[String]("play.http.secret.key").contains("changeme")) Mode.Dev else Mode.Prod

    override lazy val serverConfig: ServerConfig = ServerConfig(port = Some(port), mode = mode)

    private val logger = Logger(this.getClass).logger

    private val action = Action { request =>
      logger.info(s"method = ${request.method}")
      logger.info(s"secure = ${request.secure}")
      logger.info(s"path = ${request.path}")
      logger.info(s"headers = ${request.headers.toString}")
      logger.info(s"envs = ${sys.env.toString}")

      val logBody = request.body match {
        case r: AnyContentAsRaw => r.raw.asBytes().map(_.utf8String).toString
        case _ => request.body.toString
      }
      logger.info(logBody)

      // todo: this could be better
      request.body match {
        case c: AnyContentAsJson => Results.Ok(c.json)
        case c: AnyContentAsText => Results.Ok(c.txt)
        case AnyContentAsEmpty => Results.Ok(EmptyContent())
        case _ => Results.Ok(logBody)
      }
    }

    override lazy val router: Router = Router.from {
      case GET(_) | POST(_) | PUT(_) | DELETE(_) | PATCH(_) | OPTIONS(_) | HEAD(_) => action
    }

  }

  // server is lazy so eval it to start it
  components.server

}
