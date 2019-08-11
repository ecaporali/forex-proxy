package forex.http.rates

import cats.effect.IO
import forex.TestUtilsIO
import forex.domain.RateFixtures
import forex.http.rates.ProtocolFixtures.buildGetApiResponse
import forex.programs.rates.errors.Error.RateLookupFailed
import io.circe.syntax._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Http4sLiteralSyntax, Method, Request, Response, Status, Uri}
import org.scalatest.{FreeSpec, Matchers}

class RatesHttpRoutesSpec extends FreeSpec with Matchers with Http4sDsl[IO] with TestUtilsIO {

  "RatesHttpRoutes - successfully execute OneForge request" - {
    val ratesHttpRoutesSuccess = new RatesHttpRoutes[IO](
      handleGetRate = _ => IO(Right(RateFixtures.buildRate()))
    )

    val ratesEndpoints: List[(Request[IO], String, Status)] = List(
      (request(uri = uri"/rates?from=AUD&to=JPY", method = GET), buildGetApiResponse().asJson.noSpaces, Status.Ok),
      (request(uri = uri"/rates?from=WRONG_FROM&to=JPY", method = GET), "Unknown currency code WRONG_FROM", Status.BadRequest),
      (request(uri = uri"/rates?from=AUD&to=WRONG_TO", method = GET), "Unknown currency code WRONG_TO", Status.BadRequest)
    )

    ratesEndpoints.foreach { endpoint =>
      val (req, expectedResponse, httpStatus) = endpoint

      s"should return the correct response for endpoint: ${req.method} ${req.uri}" in {
        val response: Response[IO] = runIO(ratesHttpRoutesSuccess.routes.run(req).value).get
        response.status shouldBe httpStatus
        bodyToString(response) shouldBe expectedResponse
      }
    }
  }

  "RatesHttpRoutes - fails to execute OneForge request" - {
    val ratesHttpRoutesFailure = new RatesHttpRoutes[IO](
      handleGetRate = _ => IO(Left(RateLookupFailed("OneForge bad message!")))
    )

    val ratesEndpoint: (Request[IO], String, Status) =
      (request(uri = uri"/rates?from=AUD&to=JPY", method = GET), "OneForge bad message!", Status.InternalServerError)

    val (req, expectedResponse, httpStatus) = ratesEndpoint

    s"should return the correct response for endpoint: ${req.method} ${req.uri}" in {
      val response: Response[IO] = runIO(ratesHttpRoutesFailure.routes.run(req).value).get
      response.status shouldBe httpStatus
      bodyToString(response) shouldBe expectedResponse
    }
  }

  private def request(uri: Uri, method: Method): Request[IO] =
    Request(method = method, uri = uri)
}
