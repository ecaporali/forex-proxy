package forex.config

import org.http4s.Uri

import scala.concurrent.duration.FiniteDuration

final case class ApplicationConfig(
    http: HttpConfig,
)

final case class HttpConfig(
    server: HttpServerConfig,
    client: HttpClientConfig,
    oneForge: OneForgeConfig,
    rates: RatesConfig
)

final case class HttpServerConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

final case class HttpClientConfig(
    maxConnections: Int,
    timeout: FiniteDuration
)

final case class OneForgeConfig(
    uri: Uri,
    apiKey: String
)

final case class RatesConfig(
    cacheKeyName: String,
    priceTimeout: FiniteDuration
)
