package forex.infrastructure

import cats.effect.Sync
import cats.syntax.applicativeError._
import forex.http.CustomHeaders.JsonContentTypeHeader
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.{Header, Headers, Method, Request, Uri}
import forex.http.jsonDecoder

class HttpClient[F[_]: Sync](
    httpBlazeClient: Client[F]
) {

  def executeRequest[Response <: Product: Decoder](
      request: Request[F]
  ): F[ErrorOr[Response]] =
    httpBlazeClient
      .expect[Response](request)
      .attempt
}

object HttpClient {

  def createGetRequest[F[_]](
      uri: Uri,
      headers: Seq[Header] = List(JsonContentTypeHeader)
  ): Request[F] =
    Request[F](method = Method.GET, uri = uri)
      .withHeaders(Headers.of(headers: _*))
}
