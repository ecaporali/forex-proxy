package forex.http.rates

import cats.effect.IO
import forex.TestUtilsIO
import forex.domain.RateFixtures
import forex.http.rates.ProtocolFixtures.buildGetApiResponse
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Http4sLiteralSyntax, Method, Request, Response, Status, Uri}
import org.scalatest.{FlatSpec, Matchers}

class RatesHttpRoutesSpec extends FlatSpec with Matchers with Http4sDsl[IO] with TestUtilsIO {

  val ratesHttpRoutes = new RatesHttpRoutes[IO](
    handleGetRate = _ => IO(Right(RateFixtures.buildRate()))
  )

  val ratesEndpoint: List[(Request[IO], String, Status)] = List(
    (request(uri = uri"/rates?from=AUD&to=JPY", method = GET), buildGetApiResponse().asJson.noSpaces, Status.Ok)
  )

  ratesEndpoint.foreach { endpoint =>
    val (req, expectedResponse, httpStatus) = endpoint

    s"For endpoint: ${req.method} ${req.uri}" should "return the correct response" in {
      val response: Response[IO] = runIO(ratesHttpRoutes.routes.run(req).value).get
      response.status shouldBe httpStatus
      bodyToString(response) shouldBe expectedResponse
    }
  }

  private def request(uri: Uri, method: Method): Request[IO] =
    Request(method = method, uri = uri)
}
