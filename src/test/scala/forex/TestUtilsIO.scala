package forex

import cats.effect.IO
import org.http4s.Response
import org.http4s.util.CaseInsensitiveString

trait TestUtilsIO {

  def runIO[R](io: IO[R]): R = io.unsafeRunSync()

  def bodyToString(response: Response[IO]): String = {
    val bytes = runIO(response.body.compile.toVector)
    new String(bytes.toArray, "utf-8")
  }

  def header(response: Response[IO], headerName: String): Option[String] =
    response.headers.get(CaseInsensitiveString(headerName)).map(_.value)
}
