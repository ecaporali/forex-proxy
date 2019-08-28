package forex.config

import org.http4s.Uri

import scala.concurrent.duration._

object ConfigFixtures {

  val testApplicationConfig: ApplicationConfig = ApplicationConfig(
    HttpConfig(
      HttpServerConfig(host = "host", port = 1234, timeout = 10.seconds),
      HttpClientConfig(maxConnections = 1, timeout = 10.seconds),
      OneForgeConfig(uri = Uri.unsafeFromString("http://test.com"), apiKey = "api-key"),
      RatesConfig(cacheKeyName = "cache-key", priceTimeout = 10.seconds)
    )
  )
}
