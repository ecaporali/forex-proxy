package forex

package object infrastructure {

  type ErrorOr[A] = Throwable Either A
}
