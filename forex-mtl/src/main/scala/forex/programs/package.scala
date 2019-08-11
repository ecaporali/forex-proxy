package forex

import forex.programs.rates.errors.Error

package object programs {
  type ProgramErrorOr[A]  = Error Either A
  type RatesProgram[F[_]] = rates.Algebra[F]
  final val RatesProgram = rates.Program
}
