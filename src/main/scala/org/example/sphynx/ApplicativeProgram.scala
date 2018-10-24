package org.example.sphynx

import cats.Applicative

trait ApplicativeProgram[Alg[_[_]], A] {
  def apply[F[_]: Applicative](interp: Alg[F]): F[A]
}
