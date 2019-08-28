package forex.http

import org.http4s.Header

object CustomHeaders {
  val JsonContentTypeHeader: Header = Header("Content-Type", "application/json; charset=utf-8")
}
