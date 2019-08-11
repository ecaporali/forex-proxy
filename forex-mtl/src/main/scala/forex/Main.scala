package forex

import java.util.concurrent.Executors.newFixedThreadPool

import cats.effect._
import cats.syntax.functor._
import forex.config._
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.Json
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import scalacache.caffeine.CaffeineCache

import scala.concurrent.ExecutionContext.fromExecutor

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")

      httpClient <- BlazeClientBuilder[F](fromExecutor(newFixedThreadPool(config.http.client.maxConnections)))
        .withMaxTotalConnections(config.http.client.maxConnections)
        .withMaxConnectionsPerRequestKey(_ => config.http.client.maxConnections)
        .withMaxWaitQueueLimit(-1)
        .withRequestTimeout(config.http.client.timeout)
        .stream

      logger <- Stream.eval(Slf4jLogger.create[F])

      caffeineCache <- Stream.eval(Sync[F].delay(CaffeineCache[Map[String, Json]]))

      module = Module[F](config, httpClient, caffeineCache, logger)

      _ <- BlazeServerBuilder[F]
            .bindHttp(config.http.server.port, config.http.server.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
