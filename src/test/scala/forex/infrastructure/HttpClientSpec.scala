package forex.infrastructure

import cats.effect.IO
import cats.implicits._
import forex.TestUtilsIO
import io.circe.Json
import io.circe.Json.fromString
import org.http4s.client.Client.fromHttpApp
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpApp, Response, Uri}
import org.scalatest.{FreeSpec, Matchers}

class HttpClientSpec extends FreeSpec with Matchers with Http4sDsl[IO] with TestUtilsIO {

  "HttpClient" - {
    val app: HttpApp[IO] = HttpApp[IO](
      req =>
        req.method match {
          case GET => Response[IO](Ok).withEntity("""{"response":"OK"}""").pure[IO]
          case _   => Response[IO](MethodNotAllowed).pure[IO]
      }
    )

    val httpClient = new HttpClient[IO](fromHttpApp(app))

    "getRequest" - {

      "send a GET request and return correct response" in {
        val request = HttpClient.createGetRequest[IO](Uri.unsafeFromString("http://example-url"))
        val result  = httpClient.executeRequest[Json](request)
        runIO(result) shouldBe Right(Json.obj(("response", fromString("OK"))))
      }
    }
  }
}
